package com.tencent.bk.devops.plugin.docker.utils

import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.bk.devops.plugin.docker.pojo.DevCloudTask
import com.tencent.bk.devops.plugin.docker.pojo.TaskStatus
import com.tencent.bk.devops.plugin.docker.pojo.job.request.JobRequest
import com.tencent.bk.devops.plugin.docker.pojo.job.response.JobResponse
import com.tencent.bk.devops.plugin.docker.pojo.status.JobStatusResponse
import com.tencent.bk.devops.plugin.utils.JsonUtil
import com.tencent.bk.devops.plugin.utils.OkhttpUtils
import okhttp3.*
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.StringUtils
import java.util.*

class DevCloudClient(
    private val executeUser: String,
    private val devCloudAppId: String,
    private val devCloudUrl: String,
    private val devCloudToken: String
) {

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

    @Synchronized fun createJob(
        jobReq: JobRequest
    ): DevCloudTask {
        println("start to create job: ${jobReq.alias}, ${jobReq.clusterType}, ${jobReq.regionId}, ${jobReq.params}, ${jobReq.podNameSelector}")

        val url = "$devCloudUrl/api/v2.1/job"
        val body = JsonUtil.toJson(jobReq)
        val request = Request.Builder().url(url)
            .headers(Headers.of(getHeaders(devCloudAppId, devCloudToken, executeUser)))
            .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body)).build()
        val responseBody = OkhttpUtils.doShortHttp(request).body()!!.string()
        println("[create job] $responseBody")
        val jobRep = JsonUtil.getObjectMapper().readValue<JobResponse>(responseBody)
        if (jobRep.actionCode == 200) {
            Thread.sleep(50)        // 防止devcloud并发创建名称冲突
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
        val url = "$devCloudUrl/api/v2.1/tasks/$taskId"
//        println("get task status url: $url")
        val request = Request.Builder().url(url)
            .headers(Headers.of(getHeaders(devCloudAppId, devCloudToken, executeUser))).get().build()
        val responseBody = OkhttpUtils.doShortHttp(request).body()!!.string()
//        println("get task status response: $responseBody")
        val responseMap = JsonUtil.getObjectMapper().readValue<Map<String, Any>>(responseBody)
        if (responseMap["actionCode"] as? Int != 200) {
            throw RuntimeException("get task status fail: $responseBody")
        }
        val data = responseMap["data"] as Map<String, Any>
        return TaskStatus(data["status"] as String?, data["result"])
    }

    fun getJobStatus(
        jobName: String
    ): JobStatusResponse {
        val url = "$devCloudUrl/api/v2.1/job/$jobName/status"
//        println("job Status url: $url")
        val request = Request.Builder().url(url)
            .headers(Headers.of(getHeaders(devCloudAppId, devCloudToken, executeUser))).get().build()
        val response: Response = OkhttpUtils.doShortHttp(request)
        val body = response.body()!!.string()
//        println("[job status] $body")
        val jobStatusRep = JsonUtil.getObjectMapper().readValue<JobStatusResponse>(body)
        val actionCode: Int = jobStatusRep.actionCode
        if (actionCode != 200) {
            throw RuntimeException("get job status fail")
        }
        return jobStatusRep
    }

    fun getLog(
        jobName: String,
        sinceTime: String
    ): Pair<Boolean, String> {
        val sendUrl = "$devCloudUrl/api/v2.1/job/$jobName/logs?sinceTime=$sinceTime"
        val request = Request.Builder().url(sendUrl)
            .headers(Headers.of(getHeaders(devCloudAppId, devCloudToken, executeUser))).get().build()
        val response = OkhttpUtils.doShortHttp(request)
        val res = response.body()!!.string()
        if (!response.isSuccessful) {
            return Pair(false, res)
        }
        return try {
            val resultMap: Map<String, Any> =
                JsonUtil.getObjectMapper().readValue<HashMap<String, Any>>(res)
            val message = resultMap["message"]
            val isBlank = message is String && !StringUtils.isBlank(message as String?)
            Pair(isBlank, res)
        } catch (e: Exception) {
            Pair(false, (e.message ?: "") + "\n" + res)
        }
    }
}