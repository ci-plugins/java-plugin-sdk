package com.tencent.bk.devops.plugin.utils.script

import java.io.File
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.LogOutputStream
import org.apache.commons.exec.PumpStreamHandler
import org.slf4j.LoggerFactory

object CommandLineUtils {

    private val logger = LoggerFactory.getLogger(CommandLineUtils::class.java)

    fun execute(command: String, workspace: File?, print2Logger: Boolean, prefix: String = "", printException: Boolean = false): String {

        val result = StringBuffer()

        val cmdLine = CommandLine.parse(command)
        val executor = CommandLineExecutor()
        if (workspace != null) {
            executor.workingDirectory = workspace
        }

        val outputStream = object : LogOutputStream() {
            override fun processLine(line: String?, level: Int) {
                if (line == null)
                    return

                val tmpLine = SensitiveLineParser.onParseLine(prefix + line)
                if (print2Logger) {
                    logger.info(tmpLine)
                }
                result.append(tmpLine).append("\n")
            }
        }

        val errorStream = object : LogOutputStream() {
            override fun processLine(line: String?, level: Int) {
                if (line == null) {
                    return
                }

                val tmpLine = SensitiveLineParser.onParseLine(prefix + line)
                if (print2Logger) {
                    logger.error(tmpLine)
                }
                result.append(tmpLine).append("\n")
            }
        }
        executor.streamHandler = PumpStreamHandler(outputStream, errorStream)
        try {
            val exitCode = executor.execute(cmdLine)
            if (exitCode != 0) {
                throw RuntimeException("$prefix Script command execution failed with exit code($exitCode)")
            }
        } catch (t: Throwable) {
            if (printException) logger.warn("Fail to execute the command($command)", t)
            if (print2Logger) logger.error("$prefix Fail to execute the command($command): ${t.message}")
            throw t
        }
        return result.toString()
    }
}
