package com.tencent.bk.devops.plugin.docker

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.SdkEnv
import com.tencent.bk.devops.atom.common.Status
import com.tencent.bk.devops.atom.pojo.Result
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunResponse
import com.tencent.bk.devops.atom.utils.http.OkHttpUtils
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogResponse
import com.tencent.bk.devops.plugin.docker.utils.ParamUtils
import com.tencent.bk.devops.plugin.utils.JsonUtil
import org.apache.tools.ant.types.Commandline

object CommonExecutor {

    fun execute(
        projectId: String,
        pipelineId: String,
        buildId: String,
        request: DockerRunRequest
    ): DockerRunResponse {
        // start to run
        val runParam = getRunParamJson(request)
        val dockerHostIP = System.getenv("docker_host_ip")
        val vmSeqId = SdkEnv.getVmSeqId()
        val dockerRunUrl = "http://$dockerHostIP/api/docker/run/$projectId/$pipelineId/$vmSeqId/$buildId"
        val responseContent = OkHttpUtils.doPost(dockerRunUrl, runParam)
        val extraOptions = JsonUtil.to(responseContent, object : TypeReference<Result<Map<String, Any>>>() {}).data
        return DockerRunResponse(
            extraOptions = mapOf(
                "containerId" to extraOptions["containerId"].toString(),
                "startTimeStamp" to extraOptions["startTimeStamp"].toString()
            )
        )
    }

    fun getLogs(
        projectId: String,
        pipelineId: String,
        buildId: String,
        request: DockerRunLogRequest
    ): DockerRunLogResponse {
        val containerId = request.extraOptions.getValue("containerId")
        val startTimeStamp = request.extraOptions.getValue("startTimeStamp")
        val dockerHostIP = System.getenv("docker_host_ip")
        val vmSeqId = SdkEnv.getVmSeqId()
        val dockerGetLogUrl =
            "http://$dockerHostIP/api/docker/runlog/$projectId/$pipelineId/$vmSeqId/$buildId/$containerId/$startTimeStamp"

        val logResponse = OkHttpUtils.doGet(dockerGetLogUrl)
        val logResult = JsonUtil.to(logResponse, object : TypeReference<Result<LogParam?>>() {}).data
            ?: return DockerRunLogResponse(
                status = Status.error,
                message = "the log data is null......",
                extraOptions = request.extraOptions
            )

        return if (logResult.running != true) {
            if (logResult.exitCode == 0) {
                DockerRunLogResponse(
                    log = trimLogs(logResult.logs),
                    status = Status.success,
                    message = "the Docker Run Log is listed as follows:",
                    extraOptions = request.extraOptions
                )
            } else {
                DockerRunLogResponse(
                    log = trimLogs(logResult.logs),
                    status = Status.error,
                    message = "the Docker Run Log is listed as follows:",
                    extraOptions = request.extraOptions
                )
            }
        } else {
            DockerRunLogResponse(
                log = logResult.logs,
                status = Status.running,
                message = "get log...",
                extraOptions = request.extraOptions.plus(mapOf(
                    "startTimeStamp" to (startTimeStamp.toLong() + request.timeGap / 1000).toString()
                ))
            )
        }

    }

    private fun getRunParamJson(param: DockerRunRequest): String {
        val runParam = with(param) {
            // get user pass param

            val commandLines = mutableListOf<String>()
            command.forEach {
                commandLines.addAll(Commandline.translateCommandline(it).toList())
            }

            DockerRunParam(
                imageName,
                param.dockerLoginUsername,
                param.dockerLoginPassword,
                commandLines,
                envMap ?: mapOf()
            )
        }

        return JsonUtil.toJson(runParam)
    }

    private fun trimLogs(list: List<String>?): List<String>? {
        return list?.map {
            val split = it.split("\\s+".toRegex()).toTypedArray()
            val log = if (split.size >= 3) {
                it.substring(it.indexOf(split[2]))
            } else {
                it
            }
            log
        }
    }

    private data class DockerRunParam(
        val imageName: String?,
        val registryUser: String?,
        val registryPwd: String?,
        val command: List<String>,
        val env: Map<String, String>
    )

    data class LogParam (
        val exitCode: Int? = null,
        val logs: List<String>? = null,
        val running: Boolean? = null
    )
}