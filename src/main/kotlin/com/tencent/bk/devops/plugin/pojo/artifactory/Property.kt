package com.tencent.bk.devops.plugin.pojo.artifactory


//("版本仓库-元数据")
data class Property(
    //("元数据键", required = true)
    val key: String,
    //("元数据值", required = true)
    val value: String
)