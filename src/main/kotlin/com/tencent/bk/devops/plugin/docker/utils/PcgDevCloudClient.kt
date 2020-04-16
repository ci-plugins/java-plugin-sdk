package com.tencent.bk.devops.plugin.docker.utils

import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.bk.devops.plugin.docker.pojo.DevCloudTask
import com.tencent.bk.devops.plugin.docker.pojo.TaskStatus
import com.tencent.bk.devops.plugin.docker.pojo.job.request.PcgJobRequest
import com.tencent.bk.devops.plugin.docker.pojo.job.response.JobResponse
import com.tencent.bk.devops.plugin.docker.pojo.status.JobStatusResponse
import com.tencent.bk.devops.plugin.utils.JsonUtil
import com.tencent.bk.devops.plugin.utils.OkhttpUtils
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.RandomStringUtils
import java.util.HashMap

class PcgDevCloudClient(private val executeUser: String){

    private val host = "http://mita.server.wsd.com"

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
        jobReq: PcgJobRequest
    ): DevCloudTask {
        val url = "$host/api/devcloud/job/add"
        val body = JsonUtil.toJson(jobReq)
        println("[create job] $url")
        println("start to create job: $body")

        val request = Request.Builder().url(url)
            .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body)).build()
        println("[create job headers]: ${request.headers().toMultimap()}")
        val responseBody = OkhttpUtils.doHttp(request).body()!!.string()
        println("[create job] $responseBody")
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
        val url = "$host/api/devcloud/container/task/detail?taskId=$taskId&operator=$executeUser"
        val request = Request.Builder().url(url)
            .get().build()
        val responseBody = OkhttpUtils.doHttp(request).body()!!.string()
        val responseMap = JsonUtil.getObjectMapper().readValue<Map<String, Any>>(responseBody)
        val responseData = responseMap["data"] as Map<String, Any>
        val responseDataMsg = responseData["message"] as String
        val realResponseMap = JsonUtil.getObjectMapper().readValue<Map<String, Any>>(responseDataMsg)
        if (realResponseMap["actionCode"] as? Int != 200) {
            throw RuntimeException("get task status fail")
        }
        val data = realResponseMap["data"] as Map<String, Any>
        return TaskStatus(status = data["status"] as String?, taskId = data["taskId"] as String?, result = data["result"])
    }

    fun getJobStatus(
        jobName: String
    ): JobStatusResponse {
        val url = "$host/api/devcloud/job/status?name=$jobName&operator=$executeUser"
        println("job Status url: $url")
        val request = Request.Builder().url(url)
            .get().build()
        val response: Response = OkhttpUtils.doHttp(request)
        val body = response.body()!!.string()
        println("[job status] $body")
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
    ): String? {
        val sendUrl = "$host/api/devcloud/job/logs?sinceTime=$sinceTime&name=$jobName&operator=$executeUser"
        val request = Request.Builder().url(sendUrl)
            .get().build()
        val response = OkhttpUtils.doHttp(request)
        val res = response.body()!!.string()
        if (!response.isSuccessful) {
            println("get log fail: $res")
            return null
        }
        try {
            val resultMap: Map<String, Any> =
                JsonUtil.getObjectMapper().readValue<HashMap<String, Any>>(res)
            val data = resultMap["data"] as Map<String, String>?
            val logs = data?.values?.joinToString("\n")
            println(logs)
            return logs
        } catch (e: Exception) {
            System.err.println(e.message)
        }
        return null
    }
}