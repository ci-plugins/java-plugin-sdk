package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.utils.http.SdkUtils
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import com.tencent.bk.devops.plugin.docker.pojo.job.response.JobResponse
import com.tencent.bk.devops.plugin.docker.utils.EnvUtils
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.pojo.devcloud.DevCloudJobReq
import okhttp3.RequestBody
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

class DevCloudBuildApi : BaseApi() {

    fun createJob(devCloudJobReq: DevCloudJobReq): Result<JobResponse?> {
        val path = "/dispatch-devcloud/api/build/devcloud/job"
        devCloudJobReq.podNameSelector = EnvUtils.getHostName()
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(devCloudJobReq))
        logger.info("create job request: ${JsonUtil.toJson(requestBody)}")
        val request = buildPost(path, requestBody, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "创建devCloud job失败")
        logger.info("create job response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<JobResponse?>>() {})
    }

    fun getJobStatus(jobName: String): Result<String> {
        val path = "/dispatch-devcloud/api/build/devcloud/job/" + jobName + "status"
        val request = buildGet(path, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "获取job状态失败")
        logger.info("get job status response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<String>>() {})
    }

    fun getJobLogs(jobName: String): Result<String> {
        val path = "/dispatch-devcloud/api/build/devcloud/job/" + jobName + "logs"
        val request = buildGet(path, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "获取job日志失败")
        logger.info("get job logs response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<String>>() {})
    }

    fun getTask(taskId: String): Result<String> {
        val path = "/dispatch-devcloud/api/build/devcloud/task/" + taskId
        val request = buildGet(path, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "获取task信息失败")
        logger.info("get task response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<String>>() {})
    }

    private fun getUserId(): String {
        var inputJson: String? = null
        try {
            inputJson = FileUtils.readFileToString(
                File(SdkUtils.getDataDir() + "/" + SdkUtils.getInputFile()),
                Charset.defaultCharset()
            )
        } catch (e: IOException) {
            logger.error("parse inputJson throw Exception", e)
        }

        val inputMap: Map<String, Any> = JsonUtil.fromJson(inputJson, object : TypeReference<MutableMap<String, Any>>() {})
        logger.info("inputMap: $inputMap")
        return inputMap["pipeline.start.user.name"] as String
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DevCloudBuildApi::class.java)
    }
}
