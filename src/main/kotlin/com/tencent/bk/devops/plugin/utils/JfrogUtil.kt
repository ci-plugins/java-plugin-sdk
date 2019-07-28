package com.tencent.bk.devops.plugin.utils

import com.tencent.bk.devops.plugin.api.impl.JfrogResourceApi
import com.tencent.bk.devops.plugin.pojo.artifactory.JfrogFile
import java.nio.file.FileSystems
import java.nio.file.Paths


class JfrogUtil {
    private val jfrogResourceApi = JfrogResourceApi()

    /**
     * 根据输入匹配流水线仓库或者自定义仓库对应的文件
     * @param runningBuildId  当前流水线构建号对应的pipelineBuildId
     * @param srcPath         待匹配的文件路径，支持通配符
     * @param pipelineId      流水线id如果为"",匹配自定义仓库，不为空则是匹配对应流水线仓库的id
     * @param buildId         流水线构建id,用法同上
     * @return 匹配到的文件集合
     */
    fun matchFile(runningBuildId: String, srcPath: String, pipelineId: String = "", buildId: String = ""): List<JfrogFile> {
        val result = mutableListOf<JfrogFile>()
        val data = jfrogResourceApi.getAllFiles(runningBuildId, pipelineId, buildId)

        val matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + srcPath)
        data.files.forEach { jfrogFile ->
            if (matcher.matches(Paths.get(jfrogFile.uri.removePrefix("/")))) {
                result.add(jfrogFile)
            }
        }
        return result
    }
}