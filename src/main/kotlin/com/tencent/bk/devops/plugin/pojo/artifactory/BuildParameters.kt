package com.tencent.bk.devops.plugin.pojo.artifactory

// ("构建模型-构建参数")
data class BuildParameters(
    // ("元素值ID-标识符", required = true)
    val key: String,
    // ("元素值名称-显示用", required = true)
    val value: Any
)