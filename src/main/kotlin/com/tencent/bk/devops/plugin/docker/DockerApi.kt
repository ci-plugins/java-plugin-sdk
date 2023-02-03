package com.tencent.bk.devops.plugin.docker

import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.pojo.Result
import com.tencent.bk.devops.plugin.docker.exception.DockerRunException
import com.tencent.bk.devops.plugin.docker.exception.DockerRunLogException
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogResponse
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunResponse
import org.slf4j.LoggerFactory

open class DockerApi : BaseApi() {

    companion object {
        private val logger = LoggerFactory.getLogger(DockerApi::class.java)
    }

    /**
     *  保持java abi兼容
     */
    @JvmOverloads
    fun dockerRunCommand(
        projectId: String,
        pipelineId: String,
        buildId: String,
        param: DockerRunRequest,
        taskId: String? = null
    ): Result<DockerRunResponse> {
        try {
            val property = System.getenv("devops_slave_model")
            val jobPoolType = System.getenv("JOB_POOL")

            var response = dockerRunCustomize(projectId, pipelineId, buildId, param)

            if (response == null) {
                response = when {
                    "docker" == property -> CommonExecutor.execute(projectId, pipelineId, buildId, param, taskId)
                    "KUBERNETES" == jobPoolType -> KubernetesExecutor.execute(param)
                    "PUBLIC_DEVCLOUD" == jobPoolType -> DevCloudExecutor.execute(param)
                    else -> ThirdPartExecutor.execute(param)
                }
            }
            return Result(response)
        } catch (ignore: Exception) {
            logger.error("Failed to create job. ${param.imageName}", ignore)
            throw DockerRunException(ignore.message ?: "")
        }
    }

    open fun dockerRunCustomize(
        projectId: String,
        pipelineId: String,
        buildId: String,
        param: DockerRunRequest
    ): DockerRunResponse? {
        return null
    }

    fun dockerRunGetLog(
        projectId: String,
        pipelineId: String,
        buildId: String,
        param: DockerRunLogRequest
    ): Result<DockerRunLogResponse> {
        try {
            val property = System.getenv("devops_slave_model")
            val jobPoolType = System.getenv("JOB_POOL")

            var response = dockerRunGetLogCustomize(projectId, pipelineId, buildId, param)

            if (response == null) {
                response = when {
                    "docker" == property -> CommonExecutor.getLogs(projectId, pipelineId, buildId, param)
                    "KUBERNETES" == jobPoolType -> KubernetesExecutor.getLogs(param)
                    "PUBLIC_DEVCLOUD" == jobPoolType -> DevCloudExecutor.getLogs(param)
                    else -> ThirdPartExecutor.getLogs(param)
                }
            }

            return Result(response)
        } catch (ignore: Exception) {
            throw DockerRunLogException(ignore.message ?: "")
        }
    }

    open fun dockerRunGetLogCustomize(
        projectId: String,
        pipelineId: String,
        buildId: String,
        param: DockerRunLogRequest
    ): DockerRunLogResponse? {
        return null
    }
}
