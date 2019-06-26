package com.tencent.bk.devops.plugin.pojo.artifactory

import com.fasterxml.jackson.annotation.JsonProperty

data class JfrogFile(
        val uri: String,
        val size: Long,
        val lastModified: String,
        val folder: Boolean,
        @JsonProperty(required = false)
        val sha1: String = ""
)