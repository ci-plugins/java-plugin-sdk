package com.tencent.bk.devops.plugin.script

import com.tencent.bk.devops.plugin.utils.MachineEnvUtils
import java.io.File

object ScriptUtils {

    fun execute(
        script: String,
        dir: File,
        runtimeVariables: Map<String, String> = mapOf(),
        prefix: String = "",
        printLog: Boolean = true,
        failExit: Boolean = true
    ): String {
        return when (MachineEnvUtils.getOS()) {
            MachineEnvUtils.OSType.LINUX, MachineEnvUtils.OSType.MAC_OS -> {
                ShellUtil.execute(script, dir, runtimeVariables, prefix, printLog, failExit)
            }
            MachineEnvUtils.OSType.WINDOWS -> {
                BatScriptUtil.execute(script, runtimeVariables, dir, prefix, printLog, failExit)
            }
            else -> {
                ShellUtil.execute(script, dir, runtimeVariables, prefix, printLog, failExit)
            }
        }
    }

}