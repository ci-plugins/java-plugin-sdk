package com.tencent.bk.devops.plugin.docker

import com.tencent.bk.devops.plugin.api.impl.BcsBuildApi
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogResponse
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunResponse
import com.tencent.bk.devops.plugin.docker.pojo.bcs.DispatchBuildStatusResp
import com.tencent.bk.devops.plugin.docker.pojo.bcs.DispatchJobReq
import com.tencent.bk.devops.plugin.docker.pojo.bcs.DockerRegistry
import com.tencent.bk.devops.plugin.docker.pojo.bcs.JobParam
import com.tencent.bk.devops.plugin.docker.pojo.common.DockerStatus
import com.tencent.bk.devops.plugin.docker.pojo.status.JobStatusResponse
import com.tencent.bk.devops.plugin.docker.utils.DevCloudClient
import com.tencent.bk.devops.plugin.docker.utils.EnvUtils
import com.tencent.bk.devops.plugin.docker.utils.ParamUtils.beiJ2UTC
import javafx.beans.property.IntegerPropertyBase
import org.apache.commons.lang3.RandomUtils
import org.apache.tools.ant.types.Commandline
import org.slf4j.LoggerFactory

object BcsExecutor {
    private val VOLUME_SERVER = "volume_server"
    private val VOLUME_PATH = "volume_path"
    private val VOLUME_MOUNT_PATH = "volume_mount_path"

    private val logger = LoggerFactory.getLogger(BcsExecutor::class.java)

    fun execute(request: DockerRunRequest): DockerRunResponse {
        val startTimeStamp = System.currentTimeMillis()
        val jobRequest = getJobRequest(request)
        val task = BcsBuildApi().createJob(jobRequest).data

        val extraOptionMap = mapOf(
            "bcsTaskId" to task?.taskId.toString(),
            "bcsJobName" to jobRequest.alias,
            "startTimeStamp" to startTimeStamp.toString()
        )

        return DockerRunResponse(
            extraOptions = request.extraOptions?.plus(extraOptionMap) ?: extraOptionMap
        )
    }

    fun getLogs(param: DockerRunLogRequest): DockerRunLogResponse {
        val extraOptions = param.extraOptions.toMutableMap()

        // get task status
        val taskId = param.extraOptions["bcsTaskId"] ?: throw RuntimeException("bcsTaskId is null")
        val taskStatusFlag = param.extraOptions["taskStatusFlag"]
        if (taskStatusFlag.isNullOrBlank() || taskStatusFlag == DockerStatus.running) {
            val taskStatus = BcsBuildApi().getTask(taskId).data
            if (taskStatus.status == "failed") {
                return DockerRunLogResponse(
                    status = DockerStatus.failure,
                    message = "get task status fail",
                    extraOptions = extraOptions
                )
            }
            if (taskStatus.status != "succeeded") {
                return DockerRunLogResponse(
                    status = DockerStatus.running,
                    message = "get task status...",
                    extraOptions = extraOptions
                )
            }
        }
        extraOptions["taskStatusFlag"] = DockerStatus.success

        // get job status
        val jobStatusFlag = param.extraOptions["jobStatusFlag"]
        val jobName = param.extraOptions["bcsJobName"] ?: throw RuntimeException("bcsJobName is null")
        var jobStatusResp: DispatchBuildStatusResp? = null
        if (jobStatusFlag.isNullOrBlank() || jobStatusFlag == DockerStatus.running) {
            jobStatusResp = BcsBuildApi().getJobStatus(jobName).data
            val jobStatus = jobStatusResp.status
            if ("failed" != jobStatus && "succeeded" != jobStatus && "running" != jobStatus) {
                return DockerRunLogResponse(
                    status = DockerStatus.running,
                    message = "get job status...",
                    extraOptions = extraOptions
                )
            }
        }
        extraOptions["jobStatusFlag"] = DockerStatus.success

        // actual get log logic
        val startTimeStamp = extraOptions["startTimeStamp"]?.toInt() ?: (System.currentTimeMillis() / 1000).toInt()
        val logs = mutableListOf<String>()

        val logResult = BcsBuildApi().getJobLogs(jobName, startTimeStamp).data

        if ((logResult.log != null && logResult.log.isNotEmpty()) || logResult.errorMsg.isNullOrBlank()) {
            extraOptions["startTimeStamp"] = (startTimeStamp + param.timeGap).toString()
            logResult.log.let {
                logs.addAll(logResult.log ?: emptyList())
            }

            logResult.errorMsg.let {
                logs.add(logResult.errorMsg!!)
            }
        }

        if (jobStatusResp == null) {
            jobStatusResp = BcsBuildApi().getJobStatus(jobName).data
        }
        val finalStatus = jobStatusResp
/*        val podResults = finalStatus.data.pod_result
        podResults?.forEach { ps ->
            ps.events?.forEach { event ->
                // add logs
                logs.add(event.message)
            }
        }*/

        if (finalStatus!!.status in listOf("failed", "succeeded")) {
            val url = "/api/v2.1/job/$jobName/status"
            logger.info("final job status url: $url")
            logger.info("final job status data: $jobStatusResp")
            Thread.sleep(6000)
            val finalLogs = BcsBuildApi().getJobLogs(jobName, startTimeStamp + 6).data
            if (finalStatus.status == "failed") {
                return DockerRunLogResponse(
                    log = logs.plus(finalLogs.errorMsg ?: ""),
                    status = DockerStatus.failure,
                    message = "docker run fail...",
                    extraOptions = extraOptions
                )
            }
            return DockerRunLogResponse(
                log = logs.plus(finalLogs?.log ?: emptyList()),
                status = DockerStatus.success,
                message = "docker run success...",
                extraOptions = extraOptions
            )
        }

        return DockerRunLogResponse(
            log = logs,
            status = DockerStatus.running,
            message = "get log...",
            extraOptions = extraOptions
        )
    }

    private fun getJobRequest(param: DockerRunRequest): DispatchJobReq {
        with(param) {
            // get job param
            val cmdTmp = mutableListOf<String>()
            command.forEach {
                cmdTmp.add(it.removePrefix("\"").removeSuffix("\"").removePrefix("\'").removeSuffix("\'"))
            }
            val cmd = if (cmdTmp.size == 1) { Commandline.translateCommandline(cmdTmp.first()).toList() } else { cmdTmp }
            val jobParam = JobParam(
                env = envMap,
                command = cmd,
                labels = labels,
                ipEnabled = ipEnabled
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

            // get docker image host & path
            val imagePair = getImagePair(param.imageName)

            // get user pass param
            val registry = DockerRegistry(
                host = imagePair.first,
                username = param.dockerLoginUsername,
                password = param.dockerLoginPassword
            )

            return DispatchJobReq(
                alias = "bkdevops_job_${System.currentTimeMillis()}_${RandomUtils.nextLong()}",
                activeDeadlineSeconds = 86400,
                image = imagePair.second,
                registry = registry,
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
