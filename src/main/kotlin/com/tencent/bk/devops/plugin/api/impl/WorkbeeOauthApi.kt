package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.pojo.repository.GitToken
import com.tencent.bk.devops.plugin.utils.JsonUtil
import org.slf4j.LoggerFactory



class WorkbeeOauthApi :BaseApi() {

    companion object {
        private val logger = LoggerFactory.getLogger(WorkbeeOauthApi::class.java)
    }

    /**
     *获取工蜂OAUTH信息
     */
    fun getOauth(userId :String) : Result<GitToken?> {

        val path="/repository/api/build/oauth/git/$userId"
        val request = buildGet(path)
        val responseContent= request(request, "获取Oauth信息失败")

        return JsonUtil.to(responseContent, object : TypeReference<Result<GitToken?>>() {})

    }




}