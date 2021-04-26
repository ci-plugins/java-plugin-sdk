package com.tencent.bk.devops.plugin.pojo.devcloud

import com.fasterxml.jackson.annotation.JsonProperty

data class DevCloudJobRes (
    @JsonProperty("actionCode")
    val actionCode: Int,
    @JsonProperty("actionMessage")
    val actionMessage: String,
    @JsonProperty("data")
    val data: JobResponseData
) {
    data class JobResponseData (
        @JsonProperty("name")
        val name: String,
        @JsonProperty("taskId")
        val taskId: Int
    )
}
