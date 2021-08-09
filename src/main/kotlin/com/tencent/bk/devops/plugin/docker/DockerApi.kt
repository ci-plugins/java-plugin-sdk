package com.tencent.bk.devops.plugin.docker

import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.plugin.docker.exception.DockerRunException
import com.tencent.bk.devops.plugin.docker.exception.DockerRunLogException
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogResponse
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunResponse
import com.tencent.bk.devops.plugin.pojo.Result
import org.slf4j.LoggerFactory

open class DockerApi : BaseApi() {

    companion object {
        private val logger = LoggerFactory.getLogger(DockerApi::class.java)
    }

    open fun dockerRunCommand(projectId: String, pipelineId: String, buildId: String, param: DockerRunRequest): Result<DockerRunResponse> {
        try {
            val devCloudProperty = System.getenv("devops.slave.environment")
            val property = System.getenv("devops_slave_model")
            val newDevCloudProperty = System.getenv("DEVOPS_SLAVE_ENVIRONMENT")

//        if (null != devCloudProperty) { logger.info("devops.slave.environment: $devCloudProperty") }
//        if (null != property) { logger.info("devops_slave_model: $property") }
//        if (null != newDevCloudProperty) { logger.info("DEVOPS_SLAVE_ENVIRONMENT: $newDevCloudProperty") }

            val response = when {
                "pcg-devcloud" == newDevCloudProperty -> PcgDevCloudExecutor.execute(param)
                "DevCloud" == devCloudProperty -> DevCloudExecutor.execute(param)
                "docker" == property -> CommonExecutor.execute(projectId, pipelineId, buildId, param)
                else -> ThirdPartExecutor.execute(param)
            }
            return Result(response)
        } catch (e: Exception) {
            throw DockerRunException(e.message ?: "")
        }
    }

    open fun dockerRunGetLog(projectId: String, pipelineId: String, buildId: String, param: DockerRunLogRequest): Result<DockerRunLogResponse> {
        try {
            val devCloudProperty = System.getenv("devops.slave.environment")
            val property = System.getenv("devops_slave_model")
            val newDevCloudProperty = System.getenv("DEVOPS_SLAVE_ENVIRONMENT")

            val response = when {
                "pcg-devcloud" == newDevCloudProperty -> PcgDevCloudExecutor.getLogs(param)
                "DevCloud" == devCloudProperty -> DevCloudExecutor.getLogs(param)
                "docker" == property -> CommonExecutor.getLogs(projectId, pipelineId, buildId, param)
                else -> ThirdPartExecutor.getLogs(param)
            }
            return Result(response)
        } catch (e: Exception) {
            throw DockerRunLogException(e.message ?: "")
        }
    }
}