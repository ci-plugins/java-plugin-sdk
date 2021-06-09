package com.tencent.bk.devops.plugin.docker.utils

import java.text.SimpleDateFormat
import java.util.TimeZone

object ParamUtils {
    fun beiJ2UTC(timestamp: Long?): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'")
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(timestamp ?: System.currentTimeMillis())
    }
}
