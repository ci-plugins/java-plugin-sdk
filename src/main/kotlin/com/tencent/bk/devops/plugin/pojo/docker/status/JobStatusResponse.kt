package com.tencent.bk.devops.plugin.pojo.docker.status


data class JobStatusResponse (
    val actionCode: Int,
    val actionMessage: String,
    val data: JobStatusData
)