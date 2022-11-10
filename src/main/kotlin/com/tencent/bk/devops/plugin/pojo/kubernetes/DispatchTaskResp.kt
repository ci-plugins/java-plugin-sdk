package com.tencent.bk.devops.plugin.pojo.kubernetes

import com.fasterxml.jackson.annotation.JsonProperty

data class DispatchTaskResp(
    @JsonProperty("taskId")
    val taskId: String?,
    @JsonProperty("errorMsg")
    val errorMsg: String? = null
)
