package com.tencent.bk.devops.plugin.pojo.cos


//("CDN上传文件")
data class CdnUploadFileInfo(
    //("文件上传路径（多个路径中间逗号隔开），支持正则表达式", required = true)
    val regexPaths: String,
    //("是否自定义归档", required = true)
    val customize: Boolean,
    //("凭证ID", required = true)
    val ticketId: String,
    //("路径前缀", required = true)
    val cdnPathPrefix: String
)