package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.pojo.artifactory.BuildHistory
import com.tencent.bk.devops.plugin.pojo.artifactory.ChannelCode

class BuildResourceApi : BaseApi() {

    /**
     * 获取构建任务详情
     * @param projectId 流水线对应的项目名
     * @param pipelineId 流水线id
     * @param buildNum 流水线构建号
     * @param channelCode 流水线类型
     * @return
     */
    fun getSingleHistoryBuild(projectId: String, pipelineId: String, buildNum: String, channelCode: ChannelCode?): Result<BuildHistory?> {
        val sb = StringBuilder("/process/api/build/builds/$projectId/$pipelineId/$buildNum/history")
        if (channelCode != null) sb.append("?channelCode=${channelCode.name}")
        val path = sb.toString()
        val request = buildGet(path)
        val responseContent = request(request, "获取构建任务详情失败")
        return JsonUtil.fromJson(responseContent, object : TypeReference<Result<BuildHistory?>>() {})
    }
}