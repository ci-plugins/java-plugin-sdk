package com.tencent.bk.devops.plugin.pojo

import com.fasterxml.jackson.annotation.JsonIgnore

data class Result<out T>(
    val status: Int = 0, // 状态码，0代表成功
    val message: String? = null, // 描述信息
    val data: T? = null
) { // 数据对象

    constructor(data: T) : this(0, null, data)
    constructor(status: Int, message: String?) : this(status, message, null)

    @JsonIgnore
    fun isOk(): Boolean {
        return status == 0
    }

    @JsonIgnore
    fun isNotOk(): Boolean {
        return status != 0
    }
}
