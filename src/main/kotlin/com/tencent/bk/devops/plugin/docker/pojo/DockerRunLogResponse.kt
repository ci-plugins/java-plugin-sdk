package com.tencent.bk.devops.plugin.docker.pojo

import com.tencent.bk.devops.plugin.docker.pojo.common.DockerStatus

data class DockerRunLogResponse(
    val log: List<String>? = listOf(),
    val status: String = DockerStatus.running,
    val message: String,
    val extraOptions: Map<String, String>
) {
    override fun toString(): String {
        return "log: $log, status: $status, message: $message, " +
            "extraOptions: ${extraOptions.filter { !it.key.contains("token", ignoreCase = true) }}"
    }
}
