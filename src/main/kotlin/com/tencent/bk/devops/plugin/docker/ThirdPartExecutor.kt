package com.tencent.bk.devops.plugin.docker

import com.tencent.bk.devops.atom.common.Status
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogResponse
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunResponse
import com.tencent.bk.devops.plugin.docker.utils.ParamUtils
import com.tencent.bk.devops.plugin.docker.utils.ParamUtils.beiJ2UTC
import com.tencent.bk.devops.plugin.script.ScriptUtils
import com.tencent.bk.devops.plugin.utils.JsonUtil
import java.io.File
import java.lang.StringBuilder

object ThirdPartExecutor {
    fun execute(param: DockerRunRequest): DockerRunResponse {
        with(param) {
            val userPassPair = ParamUtils.getUserPassPair(ticketId)
            val command = "docker run -d ${getEnvVar(param.envMap)} $imageName ${command.joinToString(" && ")}"
            val containerId = ScriptUtils.execute(command, workspace)
            return DockerRunResponse(
                extraOptions = mapOf("startTimestamp" to System.currentTimeMillis().toString(),
                    "dockerContainerId" to containerId)
            )
        }
    }

    private fun getEnvVar(envMap: Map<String, String>?): String {
        val command = StringBuilder()
        envMap?.forEach {
            command.append("--env ${it.key}=${it.value} ")
        }
        return command.toString()
    }

    fun getLogs(param: DockerRunLogRequest): DockerRunLogResponse {
        val startTimestamp = param.extraOptions["startTimestamp"]?.toLong() ?: throw RuntimeException("startTimestamp is null")
        val containerId = param.extraOptions["dockerContainerId"] ?: throw RuntimeException("dockerContainerId is null")
        val startTime = beiJ2UTC(startTimestamp)
        val command = "docker logs --until=\"$startTime\" $containerId"
        val log = ScriptUtils.execute(command, param.workspace)
        return DockerRunLogResponse(
            log = listOf(log),
            status = getContainerStatus(containerId, param.workspace),
            message = "get log...",
            extraOptions = param.extraOptions.plus(mapOf(
                "startTimestamp" to (startTimestamp + 6000).toString()
            ))
        )
    }

    private fun getContainerStatus(containerId: String, workspace: File): Status {
        val inspectResult = ScriptUtils.execute("docker inspect $containerId", workspace)
        val inspectMap = JsonUtil.to<List<Map<String, Any>>>(inspectResult).first()
        val state = inspectMap["State"] as Map<String, Any>
        val status = state["Status"] as String
        if (status == "running") return Status.running

        val exitCode = state["ExitCode"] as Int
        return if (exitCode != 0) Status.failure
        else Status.success
    }

    private fun dockerLogin(param: DockerRunRequest) {
        if (param.ticketId.isNullOrBlank()) return

        val userPassPair = ParamUtils.getUserPassPair(param.ticketId)
        val username = userPassPair.first
        val password = userPassPair.second
        val loginHost = param.imageName.split("/").first()
        // WARNING! Using --password via the CLI is insecure. Use --password-stdin.
        val commandStr = "docker login $loginHost --username $username --password $password"
        println("[execute script]: " + String.format("docker login %s --username %s  --password ***", loginHost, username))
        ScriptUtils.execute(commandStr, param.workspace)
    }

    private fun dockerLogout(param: DockerRunRequest) {
        val loginHost = param.imageName.split("/").first()
        val commandStr = "docker logout $loginHost"
        println("[execute script]: $commandStr")
        ScriptUtils.execute(commandStr, param.workspace)
    }
}