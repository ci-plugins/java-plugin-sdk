package com.tencent.bk.devops.plugin.docker.pojo

data class TaskStatus(
    var status: String? = null,
    val taskId: String? = null,
    val responseBody: String? = null
)
