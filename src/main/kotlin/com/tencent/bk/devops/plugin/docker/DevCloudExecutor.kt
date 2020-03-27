package com.tencent.bk.devops.plugin.docker

import com.tencent.bk.devops.atom.common.Status
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogResponse
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunResponse
import com.tencent.bk.devops.plugin.docker.pojo.job.request.JobParam
import com.tencent.bk.devops.plugin.docker.pojo.job.request.JobRequest
import com.tencent.bk.devops.plugin.docker.pojo.job.request.Registry
import com.tencent.bk.devops.plugin.docker.utils.DevCloudClient
import com.tencent.bk.devops.plugin.docker.utils.EnvUtils
import com.tencent.bk.devops.plugin.docker.utils.ParamUtils
import com.tencent.bk.devops.plugin.docker.utils.ParamUtils.beiJ2UTC
import org.apache.commons.lang3.RandomUtils
import org.apache.tools.ant.types.Commandline
import java.text.SimpleDateFormat
import java.util.TimeZone

object DevCloudExecutor {
    private val VOLUME_SERVER = "volume_server"
    private val VOLUME_PATH = "volume_path"
    private val VOLUME_MOUNT_PATH = "volume_mount_path"

    fun execute(request: DockerRunRequest): DockerRunResponse {
        val startTimeStamp = System.currentTimeMillis()
        val jobRequest = getJobRequest(request)
        val devCloudClient = DevCloudClient(
            executeUser = request.userId,
            devCloudAppId = request.extraOptions?.get("devCloudAppId") ?: throw RuntimeException("devCloudAppId is null"),
            devCloudUrl = request.extraOptions["devCloudUrl"] ?: throw RuntimeException("devCloudUrl is null"),
            devCloudToken = request.extraOptions["devCloudToken"] ?: throw RuntimeException("devCloudToken is null")
        )
        val task = devCloudClient.createJob(jobRequest)

        return DockerRunResponse(
            extraOptions = request.extraOptions.plus(mapOf(
                "devCloudTaskId" to task.taskId.toString(),
                "devCloudJobName" to task.jobName,
                "startTimeStamp" to startTimeStamp.toString()
            ))
        )
    }

    fun getLogs(param: DockerRunLogRequest): DockerRunLogResponse {
        val extraOptions = param.extraOptions.toMutableMap()

        val devCloudClient = DevCloudClient(
            executeUser = param.userId,
            devCloudAppId = param.extraOptions["devCloudAppId"] ?: throw RuntimeException("devCloudAppId is null"),
            devCloudUrl = param.extraOptions["devCloudUrl"] ?: throw RuntimeException("devCloudUrl is null"),
            devCloudToken = param.extraOptions["devCloudToken"] ?: throw RuntimeException("devCloudToken is null")
        )

        // get task status
        val taskId = param.extraOptions["devCloudTaskId"] ?: throw RuntimeException("devCloudTaskId is null")
        val taskStatusFlag = param.extraOptions["taskStatusFlag"]
        if (taskStatusFlag.isNullOrBlank() || taskStatusFlag == Status.running.name) {
            val taskStatus = devCloudClient.getTaskStatus(taskId.toInt())
            if (taskStatus.status == "failed") {
                return DockerRunLogResponse(
                    status = Status.failure,
                    message = "get task status fail",
                    extraOptions = extraOptions
                )
            }
            if (taskStatus.status != "succeeded") {
                return DockerRunLogResponse(
                    status = Status.running,
                    message = "get task status...",
                    extraOptions = extraOptions
                )
            }
        }
        extraOptions["taskStatusFlag"] = Status.success.name

        // get job status
        val jobStatusFlag = param.extraOptions["jobStatusFlag"]
        val jobName = param.extraOptions["devCloudJobName"] ?: throw RuntimeException("devCloudJobName is null")
        if (jobStatusFlag.isNullOrBlank() || jobStatusFlag == Status.running.name) {
            val jobStatus = devCloudClient.getJobStatus(jobName).data.status
            if ("failed" != jobStatus && "succeeded" != jobStatus && "running" != jobStatus) {
                return DockerRunLogResponse(
                    status = Status.running,
                    message = "get job status...",
                    extraOptions = extraOptions
                )
            }
        }
        extraOptions["jobStatusFlag"] = Status.success.name

        // actual get log logic
        val startTimeStamp = extraOptions["startTimeStamp"]?.toLong() ?: System.currentTimeMillis()
        val logs = mutableListOf<String>()

        val logResult = devCloudClient.getLog(jobName, beiJ2UTC(startTimeStamp))

        // only if not blank then add start time
        val isNotBlank = logResult.first
        if (isNotBlank) extraOptions["startTimeStamp"] = (startTimeStamp + param.timeGap).toString()

        // add logs
        logs.add(logResult.second)

        val finalStatus = devCloudClient.getJobStatus(jobName)
        val podResults = finalStatus.data.pod_result
        podResults?.forEach { ps ->
            ps.events?.forEach { event ->
                // add logs
                logs.add(event.message)
            }
        }

        if (finalStatus.data.status in listOf("failed", "succeeded")) {
            Thread.sleep(6000)
            val finalLogs = devCloudClient.getLog(jobName, beiJ2UTC(startTimeStamp + 6000))
            if (finalStatus.data.status == "failed") {
                return DockerRunLogResponse(
                    log = logs.plus(finalLogs.second),
                    status = Status.failure,
                    message = "docker run fail...",
                    extraOptions = extraOptions
                )
            }
            return DockerRunLogResponse(
                log = logs.plus(finalLogs.second),
                status = Status.success,
                message = "docker run success...",
                extraOptions = extraOptions
            )
        }

        return DockerRunLogResponse(
            log = logs,
            status = Status.running,
            message = "get log...",
            extraOptions = extraOptions
        )
    }

    private fun getJobRequest(param: DockerRunRequest): JobRequest {
        with(param) {
            val commandLines = mutableListOf<String>()
            command.forEach {
                commandLines.addAll(Commandline.translateCommandline(it).toList())
            }

            // get job param
            val jobParam = JobParam(
                env = envMap,
                command = command
            )

            if (jobParam.nfsVolume == null) {
                val volumeServer = System.getenv(VOLUME_SERVER)
                if (!volumeServer.isNullOrBlank()) {
                    jobParam.nfsVolume = listOf(
                        JobParam.NfsVolume(
                            System.getenv(VOLUME_SERVER),
                            System.getenv(VOLUME_PATH),
                            System.getenv(VOLUME_MOUNT_PATH)
                        )
                    )
                }
            }

            if (jobParam.workDir.isNullOrBlank()) {
                jobParam.workDir = "/data/landun/workspace"
            }

            // get docker image host & path
            val imagePair = getImagePair(param.imageName)

            // get user pass param
            val registry = Registry(
                host = imagePair.first,
                username = param.dockerLoginUsername,
                password = param.dockerLoginPassword
            )

            return JobRequest(
                alias = "bkdevops_job_${System.currentTimeMillis()}_${RandomUtils.nextLong()}",
                regionId = "ap-guangzhou",
                clusterType = "normal",
                activeDeadlineSeconds = 86400,
                image = imagePair.second,
                registry = registry,
                cpu = 1,
                memory = "1024M",
                params = jobParam,
                podNameSelector = EnvUtils.getHostName()
            )
        }
    }

    private fun getImagePair(imageName: String): Pair<String, String> {
        val targetImageRepo = imageName.split("/").first()
        val targetImageName = imageName.removePrefix(targetImageRepo).removeSuffix("/")
        return Pair(targetImageRepo, targetImageName)
    }
}