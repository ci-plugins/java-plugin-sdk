package com.tencent.bk.devops.plugin.docker.pojo

import java.io.File

data class DockerRunRequest(
    val userId: String,
    val imageName: String,
    val command: List<String>,
    val dockerLoginUsername: String? = null,
    val dockerLoginPassword: String? = null,
    val envMap: Map<String, String>? = null,
    val workspace: File,
    val extraOptions: Map<String, String>? = null,
    var labels: Map<String, String>? = emptyMap(),
    var ipEnabled: Boolean? = true
) {
    override fun toString(): String {
        return "userId: $userId, imageName: $imageName, dockerLoginUsername: $dockerLoginUsername, " +
            "extraOptions: ${extraOptions?.filter { !it.key.contains("token", ignoreCase = true) }}"
    }
}
