package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.utils.http.SdkUtils
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import com.tencent.bk.devops.plugin.docker.pojo.job.request.JobRequest
import com.tencent.bk.devops.plugin.docker.pojo.job.response.JobCreateResp
import com.tencent.bk.devops.plugin.docker.pojo.job.response.JobStatusResp
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import okhttp3.RequestBody
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory

class KubernetesBuildApi : BaseApi() {

    private val logger = LoggerFactory.getLogger(KubernetesBuildApi::class.java)

    private val urlPrefix = "/dispatch-kubernetes/api/build/sdk/job"

    fun createJob(req: JobRequest): Result<JobCreateResp> {
        logger.info("start to create job: ${req.alias}, ${req.params}, ${req.podNameSelector}")

        val path = urlPrefix
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(req))

        val request = buildPost(path, requestBody, mutableMapOf("X-DEVOPS-UID" to getUserId()))
        val responseContent = request(request, "创建kubernetes job失败")

        logger.info("create job response: $responseContent")

        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<JobCreateResp>>() {})
    }

    fun getJobStatus(jobName: String): Result<JobStatusResp?> {
        var countFailed = 0
        while (true) {
            if (countFailed > 3) {
                logger.info("Request kubernetes failed 3 times, exit with exception")
                throw RuntimeException("Request kubernetes failed 3 times, exit with exception")
            }
            try {
                val url = "$urlPrefix/$jobName/status"
                val request = buildGet(url, mutableMapOf("X-DEVOPS-UID" to getUserId()))
                val responseContent = request(request, "获取kubernetes job status 失败")
                val jobStatusRep =
                    JsonUtil.fromJson(responseContent, object : TypeReference<Result<JobStatusResp?>>() {})
                if (jobStatusRep.isNotOk() || jobStatusRep.data == null) {
                    throw RuntimeException("get job status fail: $url, $responseContent")
                }
                return jobStatusRep
            } catch (e: IOException) {
                logger.info("Get kubernetes job status exception: ${e.message}")
                countFailed++
            }
        }
    }

    fun getLog(
        jobName: String,
        sinceTime: Long
    ): String? {
        var countFailed = 0
        while (true) {
            if (countFailed > 3) {
                logger.info("Request kubernetes get log failed 3 times, exit with exception")
                throw RuntimeException("Request kubernetes get log failed 3 times, exit with exception")
            }
            try {
                val url = "$urlPrefix/$jobName/logs?since=$sinceTime"
                val request = buildGet(url, mutableMapOf("X-DEVOPS-UID" to getUserId()))
                val responseContent = request(request, "获取kubernetes job logs 失败")
                val jobLogRep = JsonUtil.fromJson(responseContent, object : TypeReference<Result<String>>() {})
                if (jobLogRep.isNotOk()) {
                    return null
                }
                val logs = jobLogRep.data
                logger.info(logs)
                return logs
            } catch (e: IOException) {
                logger.info("Get kubernetes job log exception: ${e.message}")
                countFailed++
            }
        }
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

        val inputMap: Map<String, Any> =
            JsonUtil.fromJson(inputJson, object : TypeReference<MutableMap<String, Any>>() {})
        logger.info("inputMap: $inputMap")
        return inputMap["pipeline.start.user.name"] as String
    }
}
