package com.tencent.bk.devops.plugin.docker.pojo.bcs

import com.fasterxml.jackson.annotation.JsonProperty

data class DispatchTaskResp(
    @JsonProperty("taskId")
    val taskId: String?,
    @JsonProperty("errorMsg")
    val errorMsg: String? = null
)
