package com.tencent.bk.devops.plugin.pojo.docker

import com.tencent.bk.devops.plugin.pojo.docker.common.DockerStatus

data class DockerRunLogResponse(
    val log: List<String>? = listOf(),
    val status: String = DockerStatus.running,
    val message: String,
    val extraOptions: Map<String, String>
) {
    override fun toString(): String {
        return "log: $log, status: $status, message: $message, extraOptions: ${extraOptions.filter { !it.key.contains("token", ignoreCase = true) }}"
    }
}