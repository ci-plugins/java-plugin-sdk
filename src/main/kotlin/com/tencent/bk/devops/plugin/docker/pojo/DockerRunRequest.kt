package com.tencent.bk.devops.plugin.docker.pojo

import java.io.File

data class DockerRunRequest(
    val userId: String,
    val imageName: String,
    val command: List<String>,
    val ticketId: String? = null,
    val envMap: Map<String, String>? = null,
    val workspace: File,
    val extraOptions: Map<String, String>? = null
)
