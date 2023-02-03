package com.tencent.bk.devops.plugin.docker.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.bk.devops.plugin.docker.pojo.DevCloudTask
import com.tencent.bk.devops.plugin.docker.pojo.TaskStatus
import com.tencent.bk.devops.plugin.docker.pojo.job.request.JobRequest
import com.tencent.bk.devops.plugin.docker.pojo.job.response.JobResponse
import com.tencent.bk.devops.plugin.docker.pojo.status.JobStatusResponse
import com.tencent.bk.devops.plugin.utils.JsonUtil
import com.tencent.bk.devops.plugin.utils.OkhttpUtils
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

class DevCloudClient(
    private val executeUser: String,
    private val devCloudAppId: String,
    private val devCloudUrl: String,
    private val devCloudToken: String
) {

    companion object {
        private val logger = LoggerFactory.getLogger(DevCloudClient::class.java)
    }

    private fun getHeaders(
        appId: String,
        token: String,
        staffName: String
    ): Map<String, String> {
        val headerBuilder = mutableMapOf<String, String>()
        headerBuilder["APPID"] = appId
        val random = RandomStringUtils.randomAlphabetic(8)
        headerBuilder["RANDOM"] = random
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        headerBuilder["TIMESTP"] = timestamp
        headerBuilder["STAFFNAME"] = staffName
        val encKey = DigestUtils.md5Hex(token + timestamp + random)
        headerBuilder["ENCKEY"] = encKey
        return headerBuilder
    }

    fun createJob(
        jobReq: JobRequest
    ): DevCloudTask {
        logger.info("start to create job: ${jobReq.alias}, ${jobReq.clusterType}, ${jobReq.regionId}, ${jobReq.params}, ${jobReq.podNameSelector}")

        val url = "$devCloudUrl/api/v2.1/job"
        val body = JsonUtil.toJson(jobReq)
        val request = Request.Builder().url(url)
            .headers(getHeaders(devCloudAppId, devCloudToken, executeUser).toHeaders())
            .post(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), body)).build()
        val responseBody = OkhttpUtils.doShortHttp(request).body!!.string()
        logger.info("[create job] $responseBody")
        val jobRep = JsonUtil.to(responseBody, JobResponse::class.java)
        if (jobRep.actionCode == 200) {
            return DevCloudTask(
                jobRep.data.taskId,
                jobRep.data.name
            )
        } else {
            throw RuntimeException("create job fail")
        }
    }

    fun getTaskStatus(
        taskId: Int
    ): TaskStatus {
        var countFailed = 0
        while (true) {
            if (countFailed > 3) {
                logger.info("Request DevCloud failed 3 times, exit with exception")
                throw RuntimeException("Request DevCloud failed 3 times, exit with exception")
            }
            try {
                val url = "$devCloudUrl/api/v2.1/tasks/$taskId"
                logger.info("get task status url: $url")
                val request = Request.Builder().url(url)
                        .headers(getHeaders(devCloudAppId, devCloudToken, executeUser).toHeaders()).get().build()
                val responseBody = OkhttpUtils.doShortHttp(request).body!!.string()
                logger.info("get task status response: $responseBody")
                val responseMap = JsonUtil.to(responseBody, object : TypeReference<Map<String, Any>>() {})
                if (responseMap["actionCode"] as? Int != 200) {
                    throw RuntimeException("get task status fail: $responseBody")
                }
                val data = responseMap["data"] as Map<String, Any>
                return TaskStatus(status = data["status"] as String?, taskId = data["taskId"] as String?, responseBody = responseBody)
            } catch (e: IOException) {
                logger.info("Get DevCloud task status exception: ${e.message}")
                countFailed++
            }
        }
    }

    fun getJobStatus(
        jobName: String
    ): JobStatusResponse {
        var countFailed = 0
        while (true) {
            if (countFailed > 3) {
                logger.info("Request DevCloud failed 3 times, exit with exception")
                throw RuntimeException("Request DevCloud failed 3 times, exit with exception")
            }
            try {
                val url = "$devCloudUrl/api/v2.1/job/$jobName/status"
                val request = Request.Builder().url(url)
                        .headers(getHeaders(devCloudAppId, devCloudToken, executeUser).toHeaders()).get().build()
                val response: Response = OkhttpUtils.doShortHttp(request)
                val body = response.body!!.string()
                val jobStatusRep = JsonUtil.to(body, JobStatusResponse::class.java)
                val actionCode: Int = jobStatusRep.actionCode
                if (actionCode != 200) {
                    throw RuntimeException("get job status fail: $url, $body")
                }
                return jobStatusRep
            } catch (e: IOException) {
                logger.info("Get DevCloud job status exception: ${e.message}")
                countFailed++
            }
        }
    }

    fun getLog(
        jobName: String,
        sinceTime: String
    ): String? {
        var countFailed = 0
        while (true) {
            try {
                val sendUrl = "$devCloudUrl/api/v2.1/job/$jobName/logs?sinceTime=$sinceTime"
                val request = Request.Builder().url(sendUrl)
                    .headers(getHeaders(devCloudAppId, devCloudToken, executeUser).toHeaders()).get().build()
                val response = OkhttpUtils.doShortHttp(request)
                val res = response.body!!.string()
                if (!response.isSuccessful) {
                    return null
                }
                val resultMap: Map<String, Any> = JsonUtil.to(res, object : TypeReference<Map<String, Any>>() {})
                val data = resultMap["data"] as Map<String, String>?
                val logs = data?.values?.joinToString("\n")
                logger.info(logs)
                return logs
            } catch (e: IOException) {
                logger.info("Get DevCloud job log exception: ${e.message}")
                if ("timeout" == e.message) {
                    countFailed++
                    if (countFailed > 3) {
                        logger.info("Request DevCloud get log failed 3 times, exit with exception")
                        return null
                    }
                    Thread.sleep(1000L)
                } else {
                    throw RuntimeException("Request DevCloud get log failed 3 times, exit with exception")
                }
            }
        }
    }
}
