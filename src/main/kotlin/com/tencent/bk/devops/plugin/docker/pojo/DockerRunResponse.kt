package com.tencent.bk.devops.plugin.docker.pojo

data class DockerRunResponse(
    val extraOptions: Map<String, Any>
) {
    override fun toString(): String {
        return "${extraOptions.filter { !it.key.contains("token", ignoreCase = true) }}"
    }
}
