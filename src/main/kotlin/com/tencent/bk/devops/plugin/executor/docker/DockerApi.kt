package com.tencent.bk.devops.plugin.executor.docker

import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.plugin.exception.docker.DockerRunException
import com.tencent.bk.devops.plugin.exception.docker.DockerRunLogException
import com.tencent.bk.devops.plugin.pojo.docker.DockerRunLogRequest
import com.tencent.bk.devops.plugin.pojo.docker.DockerRunLogResponse
import com.tencent.bk.devops.plugin.pojo.docker.DockerRunRequest
import com.tencent.bk.devops.plugin.pojo.docker.DockerRunResponse
import org.slf4j.LoggerFactory
import com.tencent.bk.devops.atom.pojo.Result

class DockerApi : BaseApi() {

    companion object {
        private val logger = LoggerFactory.getLogger(DockerApi::class.java)
    }

    fun dockerRunCommand(projectId: String, pipelineId: String, buildId: String, param: DockerRunRequest): Result<DockerRunResponse> {
        try {
            val property = System.getenv("devops_slave_model")

            val response = when {
                "docker" == property -> CommonExecutor.execute(projectId, pipelineId, buildId, param)
                else -> ThirdPartExecutor.execute(param)
            }
            return Result(response)
        } catch (e: Exception) {
            throw DockerRunException(e.message ?: "")
        }
    }

    fun dockerRunGetLog(projectId: String, pipelineId: String, buildId: String, param: DockerRunLogRequest): Result<DockerRunLogResponse> {
        try {
            val property = System.getenv("devops_slave_model")

            val response = when {
                "docker" == property -> CommonExecutor.getLogs(projectId, pipelineId, buildId, param)
                else -> ThirdPartExecutor.getLogs(param)
            }
            return Result(response)
        } catch (e: Exception) {
            throw DockerRunLogException(e.message ?: "")
        }
    }
}