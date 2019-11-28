package com.tencent.bk.devops.plugin.pojo.repository

data class CommitResponse(
    val name: String, // 代码库别名
    val elementId: String, // 插件编号
    val records: List<CommitData> // 代码提交记录列表
)