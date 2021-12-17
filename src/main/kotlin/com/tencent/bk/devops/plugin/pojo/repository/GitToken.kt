package com.tencent.bk.devops.plugin.pojo.repository
import com.fasterxml.jackson.annotation.JsonProperty

//@ApiModel("Token模型")
data class GitToken(
//    @ApiModelProperty("鉴权token", name = "access_token")
    @JsonProperty("access_token")
    var accessToken: String = "",
//    @ApiModelProperty("刷新token", name = "refresh_token")
    @JsonProperty("refresh_token")
    var refreshToken: String = "",
//    @ApiModelProperty("token类型", name = "token_type")
    @JsonProperty("token_type")
    val tokenType: String = "",
//    @ApiModelProperty("过期时间", name = "expires_in")
    @JsonProperty("expires_in")
    val expiresIn: Long = 0L,
    val createTime: Long? = 0L
)