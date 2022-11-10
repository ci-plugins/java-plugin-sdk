package com.tencent.bk.devops.plugin.pojo.kubernetes

import com.fasterxml.jackson.annotation.JsonProperty

data class DispatchJobLogResp(
    @JsonProperty("log")
    val log: List<String>?,
    @JsonProperty("errorMsg")
    val errorMsg: String? = null
)
