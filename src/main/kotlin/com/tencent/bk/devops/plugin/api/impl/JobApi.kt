package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.pojo.job.FastPushFileRequest
import okhttp3.RequestBody

class JobApi : BaseApi() {

    fun fastPushfile(fastPushFileRequest: FastPushFileRequest): Result<Void> {
        val path = "/artifactory/api/build/bs/pushfile"
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(fastPushFileRequest))
        val request = buildPost(path, requestBody, mutableMapOf())
        val responseContent = request(request, "执行快速分发文件失败：$fastPushFileRequest")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<Void>>() {})
    }

}