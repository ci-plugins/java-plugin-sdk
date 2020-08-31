package com.tencent.bk.devops.plugin.docker.utils

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.UnknownHostException

object EnvUtils {

    private val logger = LoggerFactory.getLogger(EnvUtils::class.java)

    fun getHostName(): String {
        var hostname = System.getenv("HOSTNAME")
        if (hostname == null || StringUtils.isBlank(hostname)) {
            try {
                hostname = InetAddress.getLocalHost().hostName
            } catch (e: UnknownHostException) {
                e.printStackTrace()
            }
        }
        logger.info("env host name is: $hostname")
        return hostname
    }
}
