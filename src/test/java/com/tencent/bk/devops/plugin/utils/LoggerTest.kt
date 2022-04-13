package com.tencent.bk.devops.plugin.utils

import com.tencent.bk.devops.plugin.docker.pojo.DockerRunLogRequest
import com.tencent.bk.devops.plugin.script.ScriptUtils
import org.junit.Test
import org.slf4j.LoggerFactory
import java.io.File

/**
 * @version 1.0
 */
class LoggerTest {

    private val logger = LoggerFactory.getLogger(LoggerTest::class.java)

//    @Test
    fun printLog() {
        logger.info("This is an info log")
        logger.warn("This is a warn log")
        logger.error("This is an error log")
        logger.trace("This is a trace log")
        logger.groupStart("Group Start Title")
        logger.groupEnd("Group End Title")
        logger.infoInTag("This is an info log", "tag1")
        logger.errorInTag("This is an error log", "tag2")
    }

    @Test
    fun dockerRunLog() {
        var workspace = File("/Users/jamikxu/Downloads")
        var command = "docker run -d -v /Users/jamikxu/Downloads/jimxzcai_agent/workspace/p-db986406eb6a445595919a75bb180849/src:/Users/jamikxu/Downloads/jimxzcai_agent/workspace/p-db986406eb6a445595919a75bb180849/src:ro -v /Users/jamikxu/Downloads/jimxzcai_agent/workspace/p-db986406eb6a445595919a75bb180849/src/.temp:/Users/jamikxu/Downloads/jimxzcai_agent/workspace/p-db986406eb6a445595919a75bb180849/src/.temp:rw   mirrors.tencent.com/codecc/codecc-auto-tools-prod-cpplint-codecc-plugin:v-1631538050326 /bin/bash -c \"cd /usr/codecc/tool_scan; python3 ./sdk/src/scan.py --input=/Users/jamikxu/Downloads/jimxzcai_agent/workspace/p-db986406eb6a445595919a75bb180849/src/.temp/codecc_b-5d852553c8164ea7958721e98b7a4de8/DEVOPS_30192C2CEEDA2A34_cpplint/tool_scan_input.json --output=/Users/jamikxu/Downloads/jimxzcai_agent/workspace/p-db986406eb6a445595919a75bb180849/src/.temp/codecc_b-5d852553c8164ea7958721e98b7a4de8/DEVOPS_30192C2CEEDA2A34_cpplint/tool_scan_output.json\""
        var result = ScriptUtils.execute(command, workspace)
        var warningmsg = "WARNING: The requested image's platform (linux/amd64) does not match the detected host platform (linux/arm64/v8) and no specific platform was requested"
        if (result.contains(warningmsg)) {
            logger.error(result)
            logger.info(result.split("requested").last())
        }

    }
}
