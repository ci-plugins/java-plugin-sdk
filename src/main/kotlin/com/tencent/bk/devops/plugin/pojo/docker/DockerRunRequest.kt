package com.tencent.bk.devops.plugin.pojo.docker

import java.io.File

data class DockerRunRequest(
    val userId: String,
    val imageName: String,
    val command: List<String>,
    val dockerLoginUsername: String? = null,
    val dockerLoginPassword: String? = null,
    val envMap: Map<String, String>? = null,
    val workspace: File,
    val extraOptions: Map<String, String>? = null
) {
    override fun toString(): String {
        return "userId: $userId, imageName: $imageName, dockerLoginUsername: $dockerLoginUsername, " +
            "extraOptions: ${extraOptions?.filter { !it.key.contains("token", ignoreCase = true) }}"
    }
}
