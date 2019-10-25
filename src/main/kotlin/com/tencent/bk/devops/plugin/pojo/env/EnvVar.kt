package com.tencent.bk.devops.plugin.pojo.env

// @ApiModel("环境变量")
data class EnvVar(
//    @ApiModelProperty("变量名", required = true)
    val name: String,
//    @ApiModelProperty("变量值", required = true)
    val value: String,
//    @ApiModelProperty("是否安全变量", required = true)
    val secure: Boolean
)