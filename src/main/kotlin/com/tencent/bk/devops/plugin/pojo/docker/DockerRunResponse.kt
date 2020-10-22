package com.tencent.bk.devops.plugin.pojo.docker

data class DockerRunResponse(
    val extraOptions: Map<String, String>
) {
    override fun toString(): String {
        return "${extraOptions.filter { !it.key.contains("token", ignoreCase = true) }}"
    }
}
