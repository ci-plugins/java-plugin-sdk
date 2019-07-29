package com.tencent.bk.devops.plugin.utils

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.collections.HashMap

object CommonUtils {

    private val logger = LoggerFactory.getLogger(CommonUtils::class.java)

    fun getInnerIP(): String {
        val ipMap = getMachineIP()
        var innerIp = ipMap["eth1"]
        if (StringUtils.isBlank(innerIp)) {
            logger.error("eth1 网卡Ip为空，因此，获取eth0的网卡ip")
            innerIp = ipMap["eth0"]
        }
        if (StringUtils.isBlank(innerIp)) {
            val ipSet = ipMap.entries
            for ((_, value) in ipSet) {
                innerIp = value
                if (!StringUtils.isBlank(innerIp)) {
                    break
                }
            }
        }

        return if (StringUtils.isBlank(innerIp) || null == innerIp) "" else innerIp
    }

    fun getMachineIP(): Map<String, String> {
        logger.info("#####################Start getMachineIP")
        val allIp = HashMap<String, String>()

        try {
            val allNetInterfaces = NetworkInterface.getNetworkInterfaces() // 获取服务器的所有网卡
            if (null == allNetInterfaces) {
                logger.error("#####################getMachineIP Can not get NetworkInterfaces")
            } else {
                while (allNetInterfaces.hasMoreElements()) { // 循环网卡获取网卡的IP地址
                    val netInterface = allNetInterfaces.nextElement()
                    val netInterfaceName = netInterface.name
                    if (StringUtils.isBlank(netInterfaceName) || "lo".equals(netInterfaceName, ignoreCase = true)) { // 过滤掉127.0.0.1的IP
                        logger.info("loopback地址或网卡名称为空")
                    } else {
                        val addresses = netInterface.inetAddresses
                        while (addresses.hasMoreElements()) {
                            val ip = addresses.nextElement() as InetAddress
                            if (ip is Inet4Address && !ip.isLoopbackAddress) {
                                val machineIp = ip.hostAddress
                                logger.info("###############netInterfaceName=$netInterfaceName The Macheine IP=$machineIp")
                                allIp[netInterfaceName] = machineIp
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("获取网卡失败", e)
        }
        return allIp
    }
}