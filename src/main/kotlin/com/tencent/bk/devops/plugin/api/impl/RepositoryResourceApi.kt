package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.pojo.repository.Repository
import com.tencent.bk.devops.plugin.pojo.repository.RepositoryConfig
import com.tencent.bk.devops.plugin.utils.JsonUtil
import org.slf4j.LoggerFactory

class RepositoryResourceApi : BaseApi() {
    companion object {
        private val logger = LoggerFactory.getLogger(RepositoryResourceApi::class.java)
    }

    fun get(repositoryConfig: RepositoryConfig): Result<Repository> {
        val path =
            "/repository/api/build/repositories?" +
                "repositoryId=${repositoryConfig.getURLEncodeRepositoryId()}&" +
                "repositoryType=${repositoryConfig.repositoryType.name}"
        val request = buildGet(path)
        val responseContent = request(request, "获取代码库失败")
        return JsonUtil.to(responseContent, object : TypeReference<Result<Repository>>() {})
    }
}
