package com.tencent.bk.devops.plugin.docker.pojo

import com.tencent.bk.devops.atom.common.Status

data class DockerRunLogResponse(
    val log: List<String>? = listOf(),
    val status: Status = Status.running,
    val message: String,
    val extraOptions: Map<String, String>
)