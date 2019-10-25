package com.tencent.bk.devops.plugin.pojo.artifactory

// @ApiModel("历史构建模型")
data class BuildHistory(
    // ("构建ID", required = true)
    val id: String,
    // ("启动用户", required = true)
    val userId: String,
    // ("触发条件", required = true)
    val trigger: String,
    // ("构建号", required = true)
    val buildNum: Int?,
    // ("编排文件版本号", required = true)
    val pipelineVersion: Int,
    // ("开始时间", required = true)
    val startTime: Long,
    // ("结束时间", required = true)
    val endTime: Long?,
    // ("状态", required = true)
    val status: String,
    // ("结束原因", required = true)
    val deleteReason: String?,
    // ("服务器当前时间戳", required = true)
    val currentTimestamp: Long,
    // ("是否是手机启动", required = false)
    val isMobileStart: Boolean = false,
    // ("原材料", required = false)
    val material: List<PipelineBuildMaterial>?,
    // ("排队于", required = false)
    val queueTime: Long?,
    // ("构件列表", required = false)
    val artifactList: List<FileInfo>?,
    // ("备注", required = false)
    val remark: String?,
    // ("总耗时(秒)", required = false)
    val totalTime: Long?,
    // ("运行耗时(秒，不包括人工审核时间)", required = false)
    val executeTime: Long?,
    // ("启动参数", required = false)
    val buildParameters: List<BuildParameters>?,
    // ("WebHookType", required = false)
    val webHookType: String?,
    // ("启动类型(新)", required = false)
    val startType: String?,
    // ("推荐版本号", required = false)
    val recommendVersion: String?
)