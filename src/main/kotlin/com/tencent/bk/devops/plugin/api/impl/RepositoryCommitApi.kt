package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.pojo.repository.CommitResponse

class RepositoryCommitApi : BaseApi() {

    /**
     * 获取当前构建代码提交记录
     */
    fun getCurrentBuildCodeCommits(): Result<List<CommitResponse>> {
        val path = "/repository/api/build/commit/getCommitsByBuildId"
        val request = buildGet(path)
        val responseContent = request(request, "获取当前构建代码提交记录失败")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<List<CommitResponse>>>() {})
    }
}