package com.tencent.bk.devops.plugin.pojo.env

data class EnvSet(
        val envHashIds: List<String>,
        val nodeHashIds: List<String>,
        val ipLists: List<IpDto>
) {
    data class IpDto(
            val ip: String,
            val source: Int = 1
    )
}