package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.pojo.Result
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import org.slf4j.LoggerFactory

/**
 * BuildVariable接口类
 */
class BuildVariableApi : BaseApi() {

    fun getVariableByName(variableName: String): Result<String?> {
        val path = "/process/api/build/variable/get_build_context?contextName=$variableName&check=true"
        val request = buildGet(path)
        val responseContent = request(request, "获取变量${variableName}失败")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<String?>>() {})
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BuildVariableApi::class.java)
    }
}
