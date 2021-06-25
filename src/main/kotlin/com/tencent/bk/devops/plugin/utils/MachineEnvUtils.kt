package com.tencent.bk.devops.plugin.utils

import java.util.Locale

object MachineEnvUtils {
    fun getOS(): String {
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

    object OSType {
        const val WINDOWS = "WINDOWS"
        const val LINUX = "LINUX"
        const val MAC_OS = "MAC_OS"
        const val OTHER = "OTHER"
    }
}
