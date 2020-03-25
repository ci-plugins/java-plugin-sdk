package com.tencent.bk.devops.plugin.docker.pojo.job.request

data class Registry (
    val host: String,
    val username: String?,
    val password: String?
)