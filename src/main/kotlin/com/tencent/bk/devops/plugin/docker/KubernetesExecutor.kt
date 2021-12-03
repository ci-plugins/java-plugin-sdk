package com.tencent.bk.devops.plugin.docker

import com.tencent.bk.devops.plugin.api.impl.KubernetesBuildApi
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogResponse
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunResponse
import com.tencent.bk.devops.plugin.docker.pojo.common.DockerStatus
import com.tencent.bk.devops.plugin.docker.pojo.common.KubernetesPodStatus
import com.tencent.bk.devops.plugin.docker.pojo.job.request.JobParam
import com.tencent.bk.devops.plugin.docker.pojo.job.request.JobRequest
import com.tencent.bk.devops.plugin.docker.pojo.job.request.Registry
import com.tencent.bk.devops.plugin.docker.pojo.job.response.JobStatusResp
import com.tencent.bk.devops.plugin.docker.utils.EnvUtils
import org.apache.commons.lang3.RandomUtils
import org.apache.tools.ant.types.Commandline
import org.slf4j.LoggerFactory

object KubernetesExecutor {

    private val VOLUME_SERVER = "volume_server"
    private val VOLUME_PATH = "volume_path"
    private val VOLUME_MOUNT_PATH = "volume_mount_path"

    private val logger = LoggerFactory.getLogger(KubernetesExecutor::class.java)

    fun execute(request: DockerRunRequest): DockerRunResponse {
        val startTimeStamp = System.currentTimeMillis()
        val jobRequest = getJobRequest(request)

        val result = KubernetesBuildApi().createJob(jobRequest)
        if (result.isNotOk()) {
            throw RuntimeException("创建kubernetes job失败")
        }

        return DockerRunResponse(
            extraOptions = request.extraOptions.let {
                if (it.isNullOrEmpty()) {
                    mapOf()
                } else {
                    it
                }
            }.plus(
                mapOf(
                    "kubernetesJobName" to result.data!!.name,
                    "startTimeStamp" to startTimeStamp.toString()
                )
            )
        )
    }

    fun getLogs(param: DockerRunLogRequest): DockerRunLogResponse {
        val extraOptions = param.extraOptions.toMutableMap()

        val api = KubernetesBuildApi()

        // get job status
        val jobStatusFlag = param.extraOptions["jobStatusFlag"]
        val jobName = param.extraOptions["kubernetesJobName"] ?: throw RuntimeException("kubernetesJobName is null")
        var jobStatusResp: JobStatusResp? = null
        var jobIp = ""
        if (jobStatusFlag.isNullOrBlank() || jobStatusFlag == DockerStatus.running) {
            jobStatusResp = api.getJobStatus(jobName).data
            jobIp = jobStatusResp?.pod_result!![0].ip ?: ""
            val jobStatus = jobStatusResp.status
            if (KubernetesPodStatus.failed != jobStatus &&
                KubernetesPodStatus.successed != jobStatus &&
                KubernetesPodStatus.running != jobStatus
            ) {
                return DockerRunLogResponse(
                    status = DockerStatus.running,
                    message = "get job status...",
                    extraOptions = extraOptions
                )
            }
        }
        extraOptions["jobIp"] = jobIp
        extraOptions["jobStatusFlag"] = DockerStatus.success

        // actual get log logic
        val startTimeStamp = extraOptions["startTimeStamp"]?.apply {
            if (trim().length == 13) {
                toLong() / 1000
            } else {
                toLong()
            }
        }?.toLong() ?: (System.currentTimeMillis() / 1000)
        val logs = mutableListOf<String>()

        val logResult = api.getLog(jobName, startTimeStamp)

        // only if not blank then add start time
        val isNotBlank = logResult.isNullOrBlank()
        if (!isNotBlank) extraOptions["startTimeStamp"] = (startTimeStamp + param.timeGap).toString()

        // add logs
        if (!isNotBlank) logs.add(logResult!!)

        if (jobStatusResp == null) {
            jobStatusResp = api.getJobStatus(jobName).data
        }
        val finalStatus = jobStatusResp
        val podResults = finalStatus?.pod_result
        podResults?.forEach { ps ->
            ps.events?.forEach { event ->
                // add logs
                logs.add(event.message)
            }
        }

        if (finalStatus?.status in listOf(KubernetesPodStatus.failed, KubernetesPodStatus.successed)) {
            Thread.sleep(6000)
            val finalLogs = api.getLog(jobName, startTimeStamp + 6000)
            if (finalStatus?.status == KubernetesPodStatus.failed) {
                return DockerRunLogResponse(
                    log = logs.plus(finalLogs ?: ""),
                    status = DockerStatus.failure,
                    message = "docker run fail...",
                    extraOptions = extraOptions
                )
            }
            return DockerRunLogResponse(
                log = logs.plus(finalLogs ?: ""),
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

    private fun getJobRequest(param: DockerRunRequest): JobRequest {
        with(param) {
            // get job param
            val cmdTmp = mutableListOf<String>()
            command.forEach {
                cmdTmp.add(it.removePrefix("\"").removeSuffix("\"").removePrefix("\'").removeSuffix("\'"))
            }
            val cmd = if (cmdTmp.size == 1) {
                Commandline.translateCommandline(cmdTmp.first()).toList()
            } else {
                cmdTmp
            }
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
            val registry = Registry(
                host = imagePair.first,
                username = param.dockerLoginUsername,
                password = param.dockerLoginPassword
            )

            val request = JobRequest(
                alias = "bkdevops-job-${System.currentTimeMillis()}-${RandomUtils.nextLong()}",
                activeDeadlineSeconds = 86400,
                image = imagePair.second,
                registry = registry,
                cpu = cpu ?: 1,
                memory = memory ?: "1024M",
                params = jobParam,
                podNameSelector = EnvUtils.getHostName()
            )
            return request
        }
    }

    private fun getImagePair(imageName: String): Pair<String, String> {
        val targetImageRepo = imageName.split("/").first()
        val targetImageName = imageName.removePrefix(targetImageRepo).removeSuffix("/")
        return Pair(targetImageRepo, targetImageName)
    }
}
