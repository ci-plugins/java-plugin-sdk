package com.tencent.bk.devops.plugin.pojo.repository

enum class RepoAuthType {
    SSH,
    HTTP,
    HTTPS,
    OAUTH;

    companion object {
        fun parse(type: String?): RepoAuthType {
            return when (type) {
                "SSH" -> SSH
                "HTTP" -> HTTP
                "HTTPS" -> HTTPS
                "OAUTH" -> OAUTH
                else -> SSH
            }
        }
    }
}
