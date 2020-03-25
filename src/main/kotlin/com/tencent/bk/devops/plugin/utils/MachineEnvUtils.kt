package com.tencent.bk.devops.plugin.utils

import java.util.Locale

object MachineEnvUtils {
    fun getOS(): OSType {
        val osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH)
        return if (osName.indexOf(string = "mac") >= 0 || osName.indexOf("darwin") >= 0) {
            OSType.MAC_OS
        } else if (osName.indexOf("win") >= 0) {
            OSType.WINDOWS
        } else if (osName.indexOf("nux") >= 0) {
            OSType.LINUX
        } else {
            OSType.OTHER
        }
    }

    enum class OSType {
        WINDOWS,
        LINUX,
        MAC_OS,
        OTHER
    }
}