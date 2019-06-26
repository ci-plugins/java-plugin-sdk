package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import com.tencent.bk.devops.plugin.common.OS
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.pojo.env.EnvWithNodeCount
import com.tencent.bk.devops.plugin.pojo.env.EnvWithPermission
import com.tencent.bk.devops.plugin.pojo.env.NodeBaseInfo
import okhttp3.RequestBody

class EnviromentApi : BaseApi() {

    fun listEnvByEnvNames(envNames: List<String>): Result<List<EnvWithPermission>> {
        val path = "/environment/api/build/listRawByEnvNames"
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(envNames))
        val request = buildPost(path, requestBody, mutableMapOf())
        val responseContent = request(request, "拉取环境[$envNames]信息失败")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<List<EnvWithPermission>>>() {})
    }

    fun listUsableServerEnvs(): Result<List<EnvWithPermission>> {
        val path = "/environment/api/build/listUsableServerEnvs"
        val request = buildGet(path)
        val responseContent = request(request, "拉取项目下有权限的环境信息失败")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<List<EnvWithPermission>>>() {})
    }

    fun listNodesByEnvIds(envHashIds: List<String>): Result<List<EnvWithPermission>> {
        val path = "/environment/api/build/listNodesByEnvIds"
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(envHashIds))
        val request = buildPost(path, requestBody, mutableMapOf())
        val responseContent = request(request, "拉取${envHashIds}环境信息失败")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<List<EnvWithPermission>>>() {})
    }

    fun listRawByEnvHashIds(envHashIds: List<String>): Result<List<EnvWithPermission>> {
        val path = "/environment/api/build/listRawByEnvHashIds"
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(envHashIds))
        val request = buildPost(path, requestBody, mutableMapOf())
        val responseContent = request(request, "拉取${envHashIds}环境信息失败")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<List<EnvWithPermission>>>() {})
    }

    fun listBuildEnvs(os: OS): Result<List<EnvWithNodeCount>> {
        val path = "/environment/api/build/buildEnvs?os=${os.name}"
        val request = buildGet(path)
        val responseContent = request(request, "拉取$os 类型构建环境失败")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<List<EnvWithNodeCount>>>() {})
    }

    fun list(): Result<List<EnvWithPermission>> {
        val path = "/environment/api/build/list"
        val request = buildGet(path)
        val responseContent = request(request, "拉取项目下所有环境信息失败")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<List<EnvWithPermission>>>() {})
    }

    fun listNodesRawByHashIds(nodeHashIds: List<String>): Result<List<NodeBaseInfo>> {
        val path = "/environment/api/build/node/listRawByHashIds"
        val requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(nodeHashIds))
        val request = buildPost(path, requestBody, mutableMapOf())
        val responseContent = request(request, "拉取${nodeHashIds}节点信息失败")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<List<NodeBaseInfo>>>() {})
    }
}