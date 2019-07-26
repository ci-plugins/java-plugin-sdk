package com.tencent.bk.devops.plugin.api.impl

import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.plugin.pojo.artifactory.JfrogFilesData
import com.tencent.devops.common.api.util.JsonUtil

class JfrogResourceApi : BaseApi() {
    private val cusListFilesUrl = "/jfrog/api/build/custom/?list&deep=1&listFolders=1"
    private val listFilesUrl = "/jfrog/api/build/archive"


    /**
     *  查询指定流水线对应构建历史下所有的文件和文件夹
     *  @param runningBuildId  运行环境对应的运行流水线pipelineBuildId
     *  @param pipelineId      需要查询的流水线id
     *  @param buildId         需要查询的流水线构建id
     *  @return 查询的文件类JfrogFilesData
     */
    fun getAllFiles(runningBuildId: String, pipelineId: String, buildId: String): JfrogFilesData {

        val listFilesUrl = if (pipelineId.isNotEmpty() && buildId.isNotEmpty()) "$listFilesUrl/$pipelineId/$buildId?list&deep=1&listFolders=1"
        else cusListFilesUrl

        val request = buildGet(listFilesUrl)

        val responseContent = request(request, "获取仓库文件失败")

        return try {
            JsonUtil.getObjectMapper().readValue(responseContent, JfrogFilesData::class.java)
        } catch (e: Exception) {
            JfrogFilesData("", "", listOf())
        }
    }
}