package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.utils.http.SdkUtils
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import com.tencent.bk.devops.plugin.docker.pojo.bcs.DispatchBuildStatusResp
import com.tencent.bk.devops.plugin.docker.pojo.bcs.DispatchJobLogResp
import com.tencent.bk.devops.plugin.docker.utils.EnvUtils
import com.tencent.bk.devops.plugin.docker.pojo.bcs.DispatchJobReq
import com.tencent.bk.devops.plugin.docker.pojo.bcs.DispatchTaskResp
import com.tencent.bk.devops.plugin.pojo.Result
import okhttp3.RequestBody
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

/**
 * BCS接口类
 */
class BcsBuildApi : BaseApi() {

    fun createJob(dispatchJobReq: DispatchJobReq): Result<DispatchTaskResp?> {
        val path = "/dispatch-bcs/api/build/bcs/job"
        dispatchJobReq.copy(podNameSelector = EnvUtils.getHostName())
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(dispatchJobReq))

        val request = buildPost(path, requestBody, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "bcs job失败")
        logger.info("create bcs job response: $responseContent")

        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<DispatchTaskResp?>>() {})
    }

    fun getJobStatus(jobName: String): Result<DispatchBuildStatusResp> {
        val path = "/dispatch-bcs/api/build/bcs/job/" + jobName + "/status"
        val request = buildGet(path, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "获取job状态失败")
        logger.info("get bcs job status response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<DispatchBuildStatusResp>>() {})
    }

    fun getJobLogs(jobName: String, sinceTime: Int): Result<DispatchJobLogResp> {
        val path = "/dispatch-bcs/api/build/bcs/job/" + jobName + "/logs?sinceTime=" + sinceTime
        val request = buildGet(path, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "获取job日志失败")
        logger.info("get bcs job logs response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<DispatchJobLogResp>>() {})
    }

    fun getTask(taskId: String): Result<DispatchBuildStatusResp> {
        val path = "/dispatch-bcs/api/build/bcs/task/status?taskId=" + taskId
        val request = buildGet(path, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "获取task信息失败")
        logger.info("get bcs task response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<DispatchBuildStatusResp>>() {})
    }

    private fun getUserId(): String {
        val inputJson: String?
        try {
            inputJson = FileUtils.readFileToString(
                File(SdkUtils.getDataDir() + "/" + SdkUtils.getInputFile()),
                Charset.defaultCharset()
            )
        } catch (e: IOException) {
            logger.error("parse inputJson throw Exception", e)
            return ""
        }

        val inputMap: Map<String, Any> = JsonUtil.fromJson(inputJson, object : TypeReference<MutableMap<String, Any>>() {})
        logger.info("inputMap: $inputMap")
        return inputMap["pipeline.start.user.name"] as String
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BcsBuildApi::class.java)
    }
}
