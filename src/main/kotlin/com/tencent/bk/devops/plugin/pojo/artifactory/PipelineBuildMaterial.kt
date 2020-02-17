package com.tencent.bk.devops.plugin.pojo.artifactory

/**
 * 构建历史中的源材料
 */
data class PipelineBuildMaterial(
    val aliasName: String? = null,
    val url: String = "",
    val branchName: String? = null,
    val newCommitId: String? = null,
    val newCommitComment: String? = null,
    val commitTimes: Int? = null
)
    