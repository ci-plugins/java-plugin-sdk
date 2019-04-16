package com.tencent.bk.devops.plugin.pojo.env

data class EnvWithPermission(
//    @ApiModelProperty("环境 HashId", required = true)
        val envHashId: String,
//    @ApiModelProperty("环境名称", required = true)
        val name: String,
//    @ApiModelProperty("环境描述", required = true)
        val desc: String,
//    @ApiModelProperty("环境类型（开发环境{DEV}|测试环境{TEST}|构建环境{BUILD}）", required = true)
        val envType: String,
//    @ApiModelProperty("节点数量", required = false)
        val nodeCount: Int?,
//    @ApiModelProperty("环境变量", required = true)
        val envVars: List<EnvVar>,
//    @ApiModelProperty("创建人", required = true)
        val createdUser: String,
//    @ApiModelProperty("创建时间", required = true)
        val createdTime: Long,
//    @ApiModelProperty("更新人", required = true)
        val updatedUser: String,
//    @ApiModelProperty("更新时间", required = true)
        val updatedTime: Long,
//    @ApiModelProperty("是否可以编辑", required = false)
        val canEdit: Boolean?,
//    @ApiModelProperty("是否可以删除", required = false)
        val canDelete: Boolean?,
//    @ApiModelProperty("是否可以使用", required = false)
        val canUse: Boolean?
)