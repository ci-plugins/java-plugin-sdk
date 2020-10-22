package com.tencent.bk.devops.plugin.pojo.docker

data class TaskStatus(
    var status: String? = null,
    val taskId: String? = null,
    val responseBody: String? = null
)