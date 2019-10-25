package com.tencent.bk.devops.plugin.pojo.artifactory

/**
 * 构建历史中的源材料
 */
data class PipelineBuildMaterial(
    val aliasName: String?,
    val url: String,
    val branchName: String?,
    val newCommitId: String?,
    val newCommitComment: String?,
    val commitTimes: Int?
)

    