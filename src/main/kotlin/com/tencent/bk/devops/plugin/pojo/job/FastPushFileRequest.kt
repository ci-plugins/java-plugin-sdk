package com.tencent.bk.devops.plugin.pojo.job

import com.tencent.bk.devops.plugin.pojo.env.EnvSet

data class FastPushFileRequest(
    val userId: String,
    val fileSources: List<FileSource>,
    val fileTargetPath: String,
    val envSet: EnvSet,
    val account: String,
    val timeout: Long
) {
    data class FileSource(
        val files: List<String>,
        val envSet: EnvSet,
        val account: String
    )
}
