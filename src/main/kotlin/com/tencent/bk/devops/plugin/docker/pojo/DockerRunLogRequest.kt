package com.tencent.bk.devops.plugin.docker.pojo

import java.io.File

data class DockerRunLogRequest (
    val userId: String,
    val workspace: File,
    val extraOptions: Map<String, String> = mapOf()
)