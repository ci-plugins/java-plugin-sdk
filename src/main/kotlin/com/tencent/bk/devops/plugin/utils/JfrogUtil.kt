package com.tencent.bk.devops.plugin.utils

import com.tencent.bk.devops.plugin.api.impl.JfrogResourceApi
import com.tencent.bk.devops.plugin.pojo.artifactory.JfrogFile
import java.nio.file.FileSystems
import java.nio.file.Paths


class JfrogUtil {
    private val jfrogResourceApi = JfrogResourceApi()

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