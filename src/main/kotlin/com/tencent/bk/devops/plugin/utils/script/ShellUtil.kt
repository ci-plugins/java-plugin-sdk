/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.bk.devops.plugin.utils.script

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files

object ShellUtil {

    private const val setEnv = "setEnv(){\n" +
        "        local key=\$1\n" +
        "        local val=\$2\n" +
        "\n" +
        "        if [[ -z \"\$@\" ]]; then\n" +
        "            return 0\n" +
        "        fi\n" +
        "\n" +
        "        if ! echo \"\$key\" | grep -qE \"^[a-zA-Z_][a-zA-Z0-9_]*\$\"; then\n" +
        "            echo \"[\$key] is invalid\" >&2\n" +
        "            return 1\n" +
        "        fi\n" +
        "\n" +
        "        echo \$key=\$val  >> ##resultFile##\n" +
        "        export \$key=\"\$val\"\n" +
        "    }\n"

    private const val setGateValue = "setGateValue(){\n" +
        "        local key=\$1\n" +
        "        local val=\$2\n" +
        "\n" +
        "        if [[ -z \"\$@\" ]]; then\n" +
        "            return 0\n" +
        "        fi\n" +
        "\n" +
        "        if ! echo \"\$key\" | grep -qE \"^[a-zA-Z_][a-zA-Z0-9_]*\$\"; then\n" +
        "            echo \"[\$key] is invalid\" >&2\n" +
        "            return 1\n" +
        "        fi\n" +
        "\n" +
        "        echo \$key=\$val  >> ##gateValueFile##\n" +
        "    }\n"
    const val GATEWAY_FILE = "gatewayValueFile.ini"

    private val specialKey = listOf(".", "-")
    private val specialValue = listOf("|", "&", "(", ")")
    private val specialCharToReplace = Regex("['\n]") // --bug=75509999 Agent环境变量中替换掉破坏性字符
    private const val WORKSPACE_ENV = "WORKSPACE"

    private val logger = LoggerFactory.getLogger(ShellUtil::class.java)

    fun execute(
        script: String,
        dir: File,
        runtimeVariables: Map<String, String>,
        prefix: String = "",
        printLog: Boolean,
        failExit: Boolean
    ): String {
        return executeUnixCommand(
            command = getCommandFile(script, dir, runtimeVariables).canonicalPath,
            sourceDir = dir,
            prefix = prefix,
            printLog = printLog,
            failExit = failExit
        )
    }

    fun getCommandFile(
        script: String,
        dir: File,
        runtimeVariables: Map<String, String>,
        failExit: Boolean = true
    ): File {
        val file = Files.createTempFile("devops_script", ".sh").toFile()
        file.deleteOnExit()

        val command = StringBuilder()
        val bashStr = script.split("\n")[0]
        if (bashStr.startsWith("#!/")) {
            command.append(bashStr).append("\n")
        }

        command.append("export $WORKSPACE_ENV=${dir.absolutePath}\n")
            .append("export DEVOPS_BUILD_SCRIPT_FILE=${file.absolutePath}\n")
        val commonEnv = runtimeVariables.filter {
            !specialEnv(it.key, it.value)
        }
        if (commonEnv.isNotEmpty()) {
            commonEnv.forEach { (name, value) ->
                // --bug=75509999 Agent环境变量中替换掉破坏性字符
                val clean = value.replace(specialCharToReplace, "")
                command.append("export $name='$clean'\n")
            }
        }

        command.append(setEnv.replace("##resultFile##", File(dir, "result.log").absolutePath))
        command.append(setGateValue.replace("##gateValueFile##", File(dir, GATEWAY_FILE).absolutePath))
        command.append(script)

        file.writeText(command.toString())
        executeUnixCommand("chmod +x ${file.absolutePath}", dir, failExit = failExit)

        return file
    }

    private fun executeUnixCommand(
        command: String,
        sourceDir: File,
        prefix: String = "",
        printLog: Boolean = true,
        failExit: Boolean = true
    ): String {
        return try {
            CommandLineUtils.execute(command, sourceDir, printLog, prefix)
        } catch (ignored: Throwable) {
            logger.error("Fail to run the command $command because of error(${ignored.message})")
            if (failExit) throw ignored
            else ignored.message ?: ""
        }
    }

    private fun specialEnv(key: String, value: String): Boolean {
        specialKey.forEach {
            if (key.contains(it)) {
                return true
            }
        }

        specialValue.forEach {
            if (value.contains(it)) {
                return true
            }
        }
        return false
    }
}
