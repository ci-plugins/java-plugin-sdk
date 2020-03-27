package com.tencent.bk.devops.plugin.docker.utils

import org.apache.commons.lang3.StringUtils
import java.net.InetAddress
import java.net.UnknownHostException

object EnvUtils {
    fun getHostName(): String {
        var hostname = System.getenv("HOSTNAME")
        if (hostname == null || StringUtils.isBlank(hostname)) {
            try {
                hostname = InetAddress.getLocalHost().hostName
            } catch (e: UnknownHostException) {
                e.printStackTrace()
            }
        }
        println("env host name is: $hostname")
        return hostname
    }
}
