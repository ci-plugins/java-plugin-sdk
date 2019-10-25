package com.tencent.bk.devops.plugin.pojo.env

// @ApiModel("节点信息(权限)")
data class NodeBaseInfo(
//    @ApiModelProperty("环境 HashId", required = true)
    val nodeHashId: String,
//    @ApiModelProperty("节点 Id", required = true)
    val nodeId: String,
//    @ApiModelProperty("节点名称", required = true)
    val name: String,
//    @ApiModelProperty("IP", required = true)
    val ip: String,
//    @ApiModelProperty("节点状态", required = true)
    val nodeStatus: String,
//    @ApiModelProperty("agent状态", required = false)
    val agentStatus: Boolean?,
//    @ApiModelProperty("节点类型", required = true)
    val nodeType: String,
//    @ApiModelProperty("操作系统", required = false)
    val osName: String?,
//    @ApiModelProperty("创建人", required = true)
    val createdUser: String,
//    @ApiModelProperty("责任人", required = false)
    val operator: String?,
//    @ApiModelProperty("备份责任人", required = false)
    val bakOperator: String?,
//    @ApiModelProperty("网关地域", required = false)
    val gateway: String?,
//    @ApiModelProperty("显示名称", required = false)
    val displayName: String?
)