package com.tencent.bk.devops.plugin.docker

import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.plugin.docker.exception.DockerRunException
import com.tencent.bk.devops.plugin.docker.exception.DockerRunLogException
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogResponse
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunResponse
import com.tencent.bk.devops.atom.pojo.Result
import org.slf4j.LoggerFactory

open class DockerApi : BaseApi() {

    companion object {
        private val logger = LoggerFactory.getLogger(DockerApi::class.java)
    }

    fun dockerRunCommand(projectId: String, pipelineId: String, buildId: String,
                         param: DockerRunRequest): Result<DockerRunResponse> {
        try {
            val property = System.getenv("devops_slave_model")

            var response = dockerRunCustomize(projectId, pipelineId, buildId, param)

            if (response == null) {
                response = when {
                    "docker" == property -> CommonExecutor.execute(projectId, pipelineId, buildId, param)
                    else -> ThirdPartExecutor.execute(param)
                }
            }
            return Result(response)
        } catch (e: Exception) {
            throw DockerRunException(e.message ?: "")
        }
    }

    open fun dockerRunCustomize(
        projectId: String, pipelineId: String, buildId: String, param: DockerRunRequest): DockerRunResponse? {
        return null
    }

    fun dockerRunGetLog(projectId: String, pipelineId: String, buildId: String,
                        param: DockerRunLogRequest): Result<DockerRunLogResponse> {
        try {
            val property = System.getenv("devops_slave_model")

            var response = dockerRunGetLogCustomize(projectId, pipelineId, buildId, param)

            if (response == null) {
                response = when {
                    "docker" == property -> CommonExecutor.getLogs(projectId, pipelineId, buildId, param)
                    else -> ThirdPartExecutor.getLogs(param)
                }
            }

            return Result(response)
        } catch (e: Exception) {
            throw DockerRunLogException(e.message ?: "")
        }
    }

    open fun dockerRunGetLogCustomize(
        projectId: String, pipelineId: String, buildId: String, param: DockerRunLogRequest): DockerRunLogResponse? {
        return null
    }
}
