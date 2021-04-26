package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import com.tencent.bk.devops.plugin.docker.pojo.job.response.JobResponse
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.pojo.devcloud.DevCloudJobReq
import okhttp3.RequestBody
import org.slf4j.LoggerFactory

class DevCloudBuildApi : BaseApi() {

    fun createJob(devCloudJobReq: DevCloudJobReq): Result<JobResponse?> {
        val path = "/dispatch-devcloud/api/build/devcloud/job"
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(devCloudJobReq))
        val request = buildPost(path, requestBody, mutableMapOf())
        val responseContent = request(request, "创建devCloud job失败")
        logger.info("create job response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<JobResponse?>>() {})
    }

    fun getJobStatus(jobName: String): Result<String> {
        val path = "/dispatch-devcloud/api/build/devcloud/job/" + jobName + "status"
        val request = buildGet(path)
        val responseContent = request(request, "获取job状态失败")
        logger.info("get job status response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<String>>() {})
    }

    fun getJobLogs(jobName: String): Result<String> {
        val path = "/dispatch-devcloud/api/build/devcloud/job/" + jobName + "logs"
        val request = buildGet(path)
        val responseContent = request(request, "获取job日志失败")
        logger.info("get job logs response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<String>>() {})
    }

    fun getTask(taskId: String): Result<String> {
        val path = "/dispatch-devcloud/api/build/devcloud/task/" + taskId
        val request = buildGet(path)
        val responseContent = request(request, "获取task信息失败")
        logger.info("get task response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<String>>() {})
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DevCloudBuildApi::class.java)
    }
}
