package com.tencent.bk.devops.plugin.pojo.cos

// "COS上传文件"
data class CosUploadFileInfo(
    // ("bucket", required = true)
    val bucket: String,
    // ("凭证ID", required = true)
    val ticketId: String,
    // ("文件上传路径（多个路径中间逗号隔开），支持正则表达式", required = true)
    val regexPaths: String,
    // ("是否自定义归档", required = true)
    val customize: Boolean,
    // ("下载URL过期时间", required = false)
    val expireSeconds: Long
)