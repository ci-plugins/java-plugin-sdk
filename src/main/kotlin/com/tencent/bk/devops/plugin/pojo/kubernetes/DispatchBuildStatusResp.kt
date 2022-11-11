package com.tencent.bk.devops.plugin.pojo.kubernetes

import com.fasterxml.jackson.annotation.JsonProperty

data class DispatchBuildStatusResp(
    @JsonProperty("status")
    val status: String,
    @JsonProperty("errorMsg")
    val errorMsg: String? = null
)
