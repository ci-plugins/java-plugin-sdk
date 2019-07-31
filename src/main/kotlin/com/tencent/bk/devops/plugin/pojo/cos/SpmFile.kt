package com.tencent.bk.devops.plugin.pojo.cos


//("Spm上传文件对应的下载地址")
data class SpmFile(
    //("uploadTaskKey", required = true)
    val uploadTaskKey: String,
    //("cdnPath", required = true)
    val cdnPath: String,
    val downloadUrlList:MutableList<Map<String,String>>
)