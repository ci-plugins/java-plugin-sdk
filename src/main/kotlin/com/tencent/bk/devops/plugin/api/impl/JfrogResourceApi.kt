package com.tencent.bk.devops.plugin.api.impl

import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.pojo.jfrog.JfrogFilesData
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import com.tencent.bk.devops.plugin.pojo.Result
import org.slf4j.LoggerFactory

class JfrogResourceApi : BaseApi() {
    private val cusListFilesUrl = "/jfrog/api/build/custom/?list&deep=1&listFolders=1"
    private val listFilesUrl = "/jfrog/api/build/archive"

    /**
     *  查询指定流水线对应构建历史或自定义仓库下所有的文件和文件夹
     *  @param runningBuildId 运行环境对应的运行流水线pipelineBuildId
     *  @param pipelineId 需要查询的流水线id
     *  @param buildId 需要查询的流水线构建id
     *  @return 查询的文件类JfrogFilesData
     */
    fun getAllFiles(runningBuildId: String, pipelineId: String, buildId: String): Result<JfrogFilesData> {

        val listFilesUrl = if (pipelineId.isNotEmpty() && buildId.isNotEmpty()) "$listFilesUrl/$pipelineId/$buildId?list&deep=1&listFolders=1"
        else cusListFilesUrl
        val request = buildGet(listFilesUrl)
        val responseContent = request(request, "获取仓库文件失败")
        return try {
            Result(JsonUtil.fromJson(responseContent, JfrogFilesData::class.java))
        } catch (e: Exception) {
            Result(-1, "获取仓库文件异常")
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(JfrogResourceApi::class.java)
    }
}