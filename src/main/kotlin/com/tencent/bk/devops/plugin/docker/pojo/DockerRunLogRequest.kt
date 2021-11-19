package com.tencent.bk.devops.plugin.docker.pojo

import java.io.File

data class DockerRunLogRequest(
    val userId: String,
    val workspace: File,
    val timeGap: Long,
    val extraOptions: Map<String, String> = mapOf()
) {
    override fun toString(): String {
        return "userId: $userId, workspace: $workspace, timeGap: $timeGap, " +
            "extraOptions: ${extraOptions.filter { !it.key.contains("token", ignoreCase = true) }}"
    }
}
