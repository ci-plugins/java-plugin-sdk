package com.tencent.bk.devops.plugin.docker

import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogResponse
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunRequest
import com.tencent.bk.devops.plugin.docker.pojo.DockerRunResponse
import com.tencent.bk.devops.plugin.pojo.Result

class DockerApi : BaseApi() {

    fun dockerRunCommand(projectId: String, pipelineId: String, buildId: String, param: DockerRunRequest): Result<DockerRunResponse> {
        val devCloudProperty = System.getenv("devops.slave.environment")
        val property = System.getenv("devops_slave_model")
        val newDevCloudProperty = System.getenv("DEVOPS_SLAVE_ENVIRONMENT")

        val response = when {
            "pcg-devcloud" == newDevCloudProperty -> PcgDevCloudExecutor.execute(param)
            "DevCloud" == devCloudProperty -> DevCloudExecutor.execute(param)
            "docker" == property -> CommonExecutor.execute(projectId, pipelineId, buildId, param)
            else -> ThirdPartExecutor.execute(param)
        }
        return Result(response)
    }

    fun dockerRunGetLog(projectId: String, pipelineId: String, buildId: String, param: DockerRunLogRequest): Result<DockerRunLogResponse> {
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
    }
}