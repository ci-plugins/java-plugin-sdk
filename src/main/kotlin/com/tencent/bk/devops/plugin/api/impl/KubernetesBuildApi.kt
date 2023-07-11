package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.utils.http.SdkUtils
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import com.tencent.bk.devops.plugin.pojo.kubernetes.DispatchBuildImageReq
import com.tencent.bk.devops.plugin.pojo.kubernetes.DispatchBuildStatusResp
import com.tencent.bk.devops.plugin.pojo.kubernetes.DispatchJobLogResp
import com.tencent.bk.devops.plugin.docker.utils.EnvUtils
import com.tencent.bk.devops.plugin.pojo.kubernetes.DispatchJobReq
import com.tencent.bk.devops.plugin.pojo.kubernetes.DispatchTaskResp
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
class KubernetesBuildApi : BaseApi() {

    fun createJob(dispatchJobReq: DispatchJobReq): Result<DispatchTaskResp?> {
        val path = "/dispatch-kubernetes/api/build/job/create"
        dispatchJobReq.copy(podNameSelector = EnvUtils.getHostName())
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(dispatchJobReq))

        val request = buildPost(path, requestBody, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "kubernetes job fail")
        logger.debug("create kubernetes job response: $responseContent")

        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<DispatchTaskResp?>>() {})
    }

    fun getJobStatus(jobName: String): Result<DispatchBuildStatusResp> {
        val path = "/dispatch-kubernetes/api/build/job/" + jobName + "/status"
        val request = buildGet(path, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "failed to get job status")
        logger.debug("get kubernetes job status response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<DispatchBuildStatusResp>>() {})
    }

    fun getJobLogs(jobName: String, sinceTime: Int): Result<DispatchJobLogResp> {
        val path = "/dispatch-kubernetes/api/build/job/" + jobName + "/logs?sinceTime=" + sinceTime
        val request = buildGet(path, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "failed to get job log")
        logger.debug("get kubernetes job logs response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<DispatchJobLogResp>>() {})
    }

    fun getTask(taskId: String): Result<DispatchBuildStatusResp> {
        val path = "/dispatch-kubernetes/api/build/task/status?taskId=" + taskId
        val request = buildGet(path, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "get task info fail")
        logger.debug("get kubernetes task response: $responseContent")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<DispatchBuildStatusResp>>() {})
    }

    fun dockerBuildAndPush(dispatchBuildImageReq: DispatchBuildImageReq): Result<DispatchTaskResp?> {
        val path = "/dispatch-kubernetes/api/build/image/buildPush"
        dispatchBuildImageReq.copy(podName = EnvUtils.getHostName())
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(dispatchBuildImageReq))

        val request = buildPost(path, requestBody, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "kubernetes docker build fail")
        logger.debug("docker build response: $responseContent")

        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<DispatchTaskResp?>>() {})
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

        val inputMap: Map<String, Any> = JsonUtil.fromJson(inputJson,
                                                           object : TypeReference<MutableMap<String, Any>>() {})
        return inputMap["BK_CI_START_USER_ID"] as String
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KubernetesBuildApi::class.java)
    }
}
