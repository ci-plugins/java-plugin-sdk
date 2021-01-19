package com.tencent.bk.devops.plugin.docker.utils

import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.bk.devops.plugin.docker.pojo.DevCloudTask
import com.tencent.bk.devops.plugin.docker.pojo.TaskStatus
import com.tencent.bk.devops.plugin.docker.pojo.job.request.PcgJobRequest
import com.tencent.bk.devops.plugin.docker.pojo.job.response.JobResponse
import com.tencent.bk.devops.plugin.docker.pojo.status.JobStatusResponse
import com.tencent.bk.devops.plugin.utils.JsonUtil
import com.tencent.bk.devops.plugin.utils.OkhttpUtils
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import java.io.IOException

class PcgDevCloudClient(
    private val executeUser: String,
    private val secretId: String,
    private val secretKey: String
){

    companion object {
        private val logger = LoggerFactory.getLogger(PcgDevCloudClient::class.java)!!
    }

    private val host = "http://api.apigw.oa.com/mita_container"

    fun createJob(
        jobReq: PcgJobRequest
    ): DevCloudTask {
        val url = "$host/api/job/add"
        val body = JsonUtil.toJson(jobReq)
        logger.info("[create job] $url")
        logger.info("start to create job: $body")

        val request = Request.Builder()
            .url(url)
            .headers(getHeaders())
            .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body))
            .build()
        val response = OkhttpUtils.doShortHttp(request)
        val responseBody = response.body()!!.string()
        logger.info("[create job] ${response.headers()}")
        logger.info("[create job] $responseBody")
        val jobRep = JsonUtil.getObjectMapper().readValue<JobResponse>(responseBody)
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
                logger.info("Request pcgDevCloud failed 3 times, exit with exception")
                throw RuntimeException("Request pcgDevCloud failed 3 times, exit with exception")
            }
            try {
                val param = JsonUtil.toJson(mapOf(
                    "taskId" to taskId.toString(),
                    "operator" to executeUser
                ))
                val url = "$host/api/container/task/detail?param=$param"
                val request = Request.Builder()
                    .url(url)
                    .headers(getHeaders())
                    .get()
                    .build()
                val response = OkhttpUtils.doShortHttp(request)
                val responseBody = response.body()!!.string()
                val headers = response.headers()
                if (!response.isSuccessful || headers["X-Gateway-Code"] != "0") {
                    throw RuntimeException("get task status fail: $headers, $responseBody, $url")
                }

                val responseMap = JsonUtil.getObjectMapper().readValue<Map<String, Any>>(responseBody)
                val responseData = responseMap["data"] as Map<*, *>
                val responseDataMsg = responseData["message"] as String
                val realResponseMap = JsonUtil.getObjectMapper().readValue<Map<String, Any>>(responseDataMsg)
                if (realResponseMap["actionCode"] as? Int != 200) {
                    throw RuntimeException("get task status fail: ${response.headers()}, $responseBody, $url")
                }
                val data = realResponseMap["data"] as Map<*, *>
                return TaskStatus(status = data["status"] as String?, taskId = data["taskId"] as String?, responseBody = responseBody)
            } catch (e: IOException) {
                logger.error("Get pcgDevCloud task status exception: ${e.message}")
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
                throw RuntimeException("Request DevCloud failed 3 times, exit with exception")
            }
            try {
                val param = JsonUtil.toJson(mapOf(
                    "name" to jobName,
                    "operator" to executeUser
                ))
                val url = "$host/api/job/status?param=$param"
                logger.info("job Status url: $url")
                val request = Request.Builder()
                    .url(url)
                    .headers(getHeaders())
                    .get()
                    .build()
                val response = OkhttpUtils.doShortHttp(request)
                val body = response.body()!!.string()
                val headers = response.headers()
                logger.info("[job status] $headers, $body")
                if (!response.isSuccessful || headers["X-Gateway-Code"] != "0") {
                    throw RuntimeException("get job status fail: $headers, $body")
                }

                val jobStatusRep = JsonUtil.getObjectMapper().readValue<JobStatusResponse>(body)
                val actionCode: Int = jobStatusRep.actionCode
                if (actionCode != 200) {
                    throw RuntimeException("get job status fail")
                }
                return jobStatusRep
            } catch (e: IOException) {
                logger.info("Get pcgDevCloud task status exception: ${e.message}")
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
            if (countFailed > 3) {
                logger.info("Request pcgDevCloud failed 3 times, exit with exception")
                throw RuntimeException("Request pcgDevCloud failed 3 times, exit with exception")
            }
            try {
                val param = JsonUtil.toJson(mapOf(
                    "sinceTime" to sinceTime,
                    "name" to jobName,
                    "operator" to executeUser
                ))
                val sendUrl = "$host/api/job/logs?param=$param"
                val request = Request.Builder()
                    .url(sendUrl)
                    .headers(getHeaders())
                    .get()
                    .build()
                val response = OkhttpUtils.doShortHttp(request)
                val res = response.body()!!.string()
                val headers = response.headers()
                if (!response.isSuccessful || headers["X-Gateway-Code"] != "0") {
                    logger.error("get log fail: $headers, $res")
                    return null
                }

                val resultMap: Map<String, Any> =
                        JsonUtil.getObjectMapper().readValue<HashMap<String, Any>>(res)
                val data = resultMap["data"] as Map<*, *>?
                val logs = data?.values?.joinToString("\n")
                logger.info(logs)
                return logs
            } catch (e: IOException) {
                logger.error("Get pcgDevCloud job log exception: ${e.message}")
                countFailed++
            }
        }
    }

    private fun getHeaders(): Headers {
        val headers = mutableMapOf<String, String>()
        headers["X-Gateway-Stage"] = "RELEASE"
        headers["X-Gateway-SecretId"] = secretId
        headers["X-Gateway-SecretKey"] = secretKey
        return Headers.of(headers)
    }
}