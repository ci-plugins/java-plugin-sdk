package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.pojo.Result
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import org.slf4j.LoggerFactory

/**
 * Stream接口类
 */
class StreamBuildApi : BaseApi() {

    fun getContextByName(contextName: String): Result<String?> {
        val path = "/process/api/build/variable/get_build_context?contextName=$contextName"
        val request = buildGet(path)
        val responseContent = request(request, "获取构建上下文${contextName}失败")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<String?>>() {})
    }

    companion object {
        private val logger = LoggerFactory.getLogger(StreamBuildApi::class.java)
    }
}
