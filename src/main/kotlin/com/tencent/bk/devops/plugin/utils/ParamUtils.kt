package com.tencent.bk.devops.plugin.utils

import com.tencent.bk.devops.atom.api.impl.CredentialApi
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.TimeZone

object ParamUtils {
    private val credentialApi = CredentialApi()

    private val logger = LoggerFactory.getLogger(ParamUtils::class.java)

    fun getEnvMap(envStr: String?): Map<String, String> {
        val envMap = mutableMapOf<String, String>()
        envStr?.split("\n")?.forEach {
            logger.info(it)

            if (!it.contains("=")) {
                logger.error("$it is illegal param,please check your input, like xx=xxx")
            } else {
                val list = it.split("=")
                envMap[list.first()] = list.last()
            }
        }
        return envMap
    }

    fun getUserPassPair(ticketId: String?): Pair<String?, String?> {
        return if (ticketId != null) {
            val map = credentialApi.getCredential(ticketId).data
            return Pair(map["username"], map["password"])
        } else {
            Pair(null, null)
        }
    }

    fun beiJ2UTC(timestamp: Long?): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'")
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(timestamp ?: System.currentTimeMillis())
    }
}
