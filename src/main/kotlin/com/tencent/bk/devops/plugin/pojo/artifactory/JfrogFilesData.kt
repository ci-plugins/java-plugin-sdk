package com.tencent.bk.devops.plugin.pojo.artifactory

data class JfrogFilesData(
        val uri: String,
        val created: String,
        val files: List<JfrogFile>
)

