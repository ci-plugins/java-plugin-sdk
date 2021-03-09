package com.tencent.bk.devops.plugin.docker

import com.tencent.bk.devops.atom.common.Status
import com.tencent.bk.devops.plugin.docker.exception.DockerPullException
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogResponse
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunResponse
import com.tencent.bk.devops.plugin.docker.pojo.common.DockerStatus
import com.tencent.bk.devops.plugin.docker.utils.ParamUtils.beiJ2UTC
import com.tencent.bk.devops.plugin.script.ScriptUtils
import com.tencent.bk.devops.plugin.utils.JsonUtil
import com.tencent.bk.devops.plugin.utils.MachineEnvUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.StringBuilder

object ThirdPartExecutor {

    private val logger = LoggerFactory.getLogger(ThirdPartExecutor::class.java)

    fun execute(param: DockerRunRequest): DockerRunResponse {
        with(param) {
            try {
                dockerLogin(this)

                doDockerPull(this)

                val containerId = doDockerRun(this)

                return DockerRunResponse(
                    extraOptions = mapOf("startTimestamp" to System.currentTimeMillis().toString(),
                        "dockerContainerId" to containerId)
                )
            } finally {
                dockerLogout(this)
            }
        }
    }

    private fun doDockerRun(param: DockerRunRequest): String {
        val dockerWorkspace = if (MachineEnvUtils.getOS() == MachineEnvUtils.OSType.WINDOWS) {
            "/" + param.workspace.canonicalPath.replace("\\", "/").replace(":", "")
        } else {
            param.workspace.canonicalPath
        }

        val command = "docker run -d -v $dockerWorkspace:$dockerWorkspace ${getEnvVar(param.envMap)} " +
            "${param.imageName} ${param.command.joinToString(" ")}"
        logger.info("execute command: $command")
        return ScriptUtils.execute(command, param.workspace)
    }

    @Synchronized
    private fun doDockerPull(param: DockerRunRequest) {
        try {
            val pullCmd = "docker pull ${param.imageName}"
            ScriptUtils.execute(pullCmd, param.workspace)
        } catch (e: Exception) {
            throw DockerPullException(e.message ?: "")
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

        val statusPair = getContainerStatus(containerId, param.workspace)
        val status = statusPair.first
        val startTime = if (status == DockerStatus.running) beiJ2UTC(startTimestamp) else statusPair.second
        val preStartTime = beiJ2UTC(startTimestamp - param.timeGap)

        var failToGetLog = false
        var errorMessage = ""
        var log = try {
            val command = "docker logs --until=\"$startTime\" --since=\"$preStartTime\" $containerId"
            ScriptUtils.execute(command, param.workspace, printLog = false)
        } catch (e :Exception) {
            errorMessage = e.message ?: ""
            failToGetLog = true
            ""
        }

        if (status != DockerStatus.running) {
            if (failToGetLog) {
                logger.error("fail to get log: $errorMessage")

                val command = "docker logs $containerId"
                log = ScriptUtils.execute(command, param.workspace, printLog = false, failExit = false)
            }
            dockerRm(containerId, param.workspace)
        }

        return DockerRunLogResponse(
            log = listOf(log),
            status = status,
            message = "get log...",
            extraOptions = param.extraOptions.plus(mapOf(
                "startTimestamp" to (startTimestamp + param.timeGap).toString()
            ))
        )
    }

    private fun getContainerStatus(containerId: String, workspace: File): Pair<String, String> {
        val inspectResult = ScriptUtils.execute(script = "docker inspect $containerId", dir = workspace, printLog = false)
        val inspectMap = JsonUtil.to<List<Map<String, Any>>>(inspectResult).first()
        val state = inspectMap["State"] as Map<String, Any>
        val status = state["Status"] as String
        if (status == "running") return Pair(DockerStatus.running, "")

        val exitCode = state["ExitCode"] as Int

        val finishedAt = state["FinishedAt"] as String

        return if (exitCode != 0) Pair(DockerStatus.failure, finishedAt)
        else Pair(DockerStatus.success, finishedAt)
    }

    private fun dockerRm(containerId: String, workspace: File) {
        val cmd = "docker rm $containerId"
        logger.info("[execute script]: $cmd")
        ScriptUtils.execute(script = cmd, dir = workspace, failExit = false)
    }

    @Synchronized
    private fun dockerLogin(param: DockerRunRequest) {
        if (param.dockerLoginUsername.isNullOrBlank()) return

        val username = param.dockerLoginUsername
        val password = param.dockerLoginPassword
        val loginHost = param.imageName.split("/").first()
        // WARNING! Using --password via the CLI is insecure. Use --password-stdin.
        val commandStr = "docker login $loginHost --username $username --password $password"
        logger.info("[execute script]: " + String.format("docker login %s --username %s  --password ***", loginHost, username))
        ScriptUtils.execute(commandStr, param.workspace)
    }

    @Synchronized
    private fun dockerLogout(param: DockerRunRequest) {
        if (param.dockerLoginUsername.isNullOrBlank()) return

        val loginHost = param.imageName.split("/").first()
        val commandStr = "docker logout $loginHost"
        logger.info("[execute script]: $commandStr")
        ScriptUtils.execute(commandStr, param.workspace)
    }
}