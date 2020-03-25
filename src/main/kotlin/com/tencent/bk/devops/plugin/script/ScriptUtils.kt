package com.tencent.bk.devops.plugin.script

import com.tencent.bk.devops.plugin.utils.MachineEnvUtils
import java.io.File

object ScriptUtils {

    fun execute(
        script: String,
        dir: File,
        runtimeVariables: Map<String, String> = mapOf(),
        prefix: String = ""
    ): String {
        return when (MachineEnvUtils.getOS()) {
            MachineEnvUtils.OSType.LINUX, MachineEnvUtils.OSType.MAC_OS -> {
                ShellUtil.execute(script, dir, runtimeVariables, prefix)
            }
            MachineEnvUtils.OSType.WINDOWS -> {
                BatScriptUtil.execute(script, runtimeVariables, dir, prefix)
            }
            else -> {
                ShellUtil.execute(script, dir, runtimeVariables, prefix)
            }
        }
    }

}