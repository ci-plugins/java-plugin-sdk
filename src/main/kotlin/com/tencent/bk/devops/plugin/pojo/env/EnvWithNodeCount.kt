package com.tencent.bk.devops.plugin.pojo.env

// @ApiModel("环境信息-Node数量")
data class EnvWithNodeCount(
//    @ApiModelProperty("环境 HashId", required = true)
    val envHashId: String,
//    @ApiModelProperty("环境名称", required = true)
    val name: String,
//    @ApiModelProperty("正常节点数量", required = true)
    val normalNodeCount: Int,
//    @ApiModelProperty("异常节点数量", required = true)
    val abnormalNodeCount: Int
)