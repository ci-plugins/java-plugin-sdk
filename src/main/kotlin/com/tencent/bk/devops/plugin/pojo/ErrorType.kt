package com.tencent.bk.devops.plugin.pojo

enum class ErrorType(val typeName: String, val num: Int) {
    USER("用户配置错误", 1), // 1 用户配置报错
    THIRD_PARTY("第三方系统错误", 2), // 2 第三方系统接入错误
    PLUGIN("插件执行错误", 3); // 3 插件执行错误

    companion object {

        fun getErrorType(name: String): ErrorType? {
            values().forEach { enumObj ->
                if (enumObj.name == name) {
                    return enumObj
                }
            }
            return null
        }

        fun getErrorType(ordinal: Int?): ErrorType {
            return when (ordinal) {
                1 -> USER
                2 -> THIRD_PARTY
                else -> PLUGIN
            }
        }
    }
}
