package com.tencent.bk.devops.plugin.docker.pojo.bcs

data class DispatchJobLogResp(
    val log: List<String>?,
    val errorMsg: String? = null
)
