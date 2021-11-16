package com.tencent.bk.devops.plugin.docker.pojo.job.request

data class JobRequest(
    val alias: String? = null,
    val activeDeadlineSeconds: Int? = null,
    val image: String? = null,
    val registry: Registry? = null,
    val cpu: Int? = null,
    val memory: String? = null,
    val params: JobParam? = null,
    val podNameSelector: String? = null
)
