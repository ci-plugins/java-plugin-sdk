package com.tencent.bk.devops.plugin.utils

import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * @version 1.0
 */
class LoggerTest {

    private val logger = LoggerFactory.getLogger(LoggerTest::class.java)

    @Test
    fun printLog() {
        logger.info("hhhhhh")
        logger.warn("wwwwww")
        logger.error("eeeee")
        logger.trace("1111")
    }
}
