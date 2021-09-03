package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.utils.JsonUtil
import okhttp3.RequestBody

class AtomMetricsApi : BaseApi() {

    fun reportMetrics(atomCode: String, data: String): Result<Boolean> {
        val path = "/monitoring/api/build/atom/metrics/report/$atomCode"
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, data)
        val request = buildPost(path, requestBody, mutableMapOf())
        val responseContent = request(request, "上报插件度量信息失败")
        return JsonUtil.to(responseContent, object : TypeReference<Result<Boolean>>() {})
    }
}
