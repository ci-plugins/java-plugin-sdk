package com.tencent.bk.devops.plugin.pojo.notify

import com.fasterxml.jackson.annotation.JsonValue

enum class EnumEmailFormat(private val format: Int) {
    PLAIN_TEXT(0),
    HTML(1);

    @JsonValue
    fun getValue(): Int {
        return format
    }

    companion object {
        fun parse(value: Int?): EnumEmailFormat {
            values().forEach { format ->
                if (format.getValue() == value) {
                    return format
                }
            }
            return PLAIN_TEXT
        }
    }
}