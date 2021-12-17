package com.tencent.bk.devops.plugin.pojo.repository
import com.fasterxml.jackson.annotation.JsonProperty

//Token模型
data class GitToken(
    @JsonProperty("access_token")
    var accessToken: String = "",//鉴权token
    @JsonProperty("refresh_token")
    var refreshToken: String = "",//刷新token
    @JsonProperty("token_type")
    val tokenType: String = "",//token类型
    @JsonProperty("expires_in")
    val expiresIn: Long = 0L,//过期时间
    val createTime: Long? = 0L
)