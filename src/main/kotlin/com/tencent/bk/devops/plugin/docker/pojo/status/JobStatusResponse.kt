package com.tencent.bk.devops.plugin.docker.pojo.status


data class JobStatusResponse (
    val actionCode: Int,
    val actionMessage: String,
    val data: JobStatusData
)