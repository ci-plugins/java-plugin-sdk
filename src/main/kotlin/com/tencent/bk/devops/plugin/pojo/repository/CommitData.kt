package com.tencent.bk.devops.plugin.pojo.repository

data class CommitData(
    val type: Short = 2, // 类型： 1-svn, 2-git, 3-gitlab
    val pipelineId: String = "", // 流水线ID
    val buildId: String = "", // 构建ID
    val commit: String = "", // 代码提交hash值
    val committer: String = "", // 代码提交者
    val commitTime: Long = 0, // 提交时间
    val comment: String? = null, // 注释
    val repoId: String? = null, // 代码库ID
    val repoName: String? = null, // 代码库编号
    val elementId: String = "", // 插件编号
    var url: String? = null // 代码提交记录url
)