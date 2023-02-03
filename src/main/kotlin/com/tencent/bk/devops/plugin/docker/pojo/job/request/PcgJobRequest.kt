package com.tencent.bk.devops.plugin.docker.pojo.job.request


data class PcgJobRequest(
    val alias: String? = null,
    val regionId: String,
    val clusterType: String? = null,
    val activeDeadlineSeconds: Int? = null,
    val image: String? = null,
    val registry: Registry? = null,
    val cpu: Int? = null,
    val memory: String? = null,
    val params: JobParam? = null,
    val podNameSelector: String? = null,
    val operator: String? = null
)
