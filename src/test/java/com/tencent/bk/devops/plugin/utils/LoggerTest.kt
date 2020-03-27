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
        logger.info("This is an info log")
        logger.warn("This is a warn log")
        logger.error("This is an error log")
        logger.trace("This is a trace log")
        logger.groupStart("Group Start Title")
        logger.groupEnd("Group End Title")
    }
}
