package com.tencent.bk.devops.plugin.docker.pojo.bcs

data class DispatchBuildStatusResp(
    val status: String,
    val errorMsg: String? = null
)
