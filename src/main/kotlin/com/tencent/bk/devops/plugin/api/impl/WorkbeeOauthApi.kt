package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.plugin.utils.JsonUtil
import org.slf4j.LoggerFactory
import java.text.MessageFormat


class WorkbeeOauthApi :BaseApi() {

    companion object {
        private val logger = LoggerFactory.getLogger(WorkbeeOauthApi::class.java)
    }

    /**
     *获取工蜂OAUTH信息
     */
    fun getOauth(userId :String) : com.tencent.bk.devops.plugin.pojo.Result<Map<String, String>> {

        val path :String=MessageFormat.format("/repository/api/build/oauth/git/{0}",userId)
        val request = buildGet(path)
        val responseContent= request(request, "获取Oauth信息失败")

        return JsonUtil.to(responseContent, object : TypeReference<com.tencent.bk.devops.plugin.pojo.Result<Map<String, String>>>() {})

    }




}