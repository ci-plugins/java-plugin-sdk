package com.tencent.bk.devops.plugin.utils

import com.fasterxml.jackson.core.type.TypeReference
import org.junit.Assert
import org.junit.Test

/**
 * @version 1.0
 */
class JsonUtilTest {

    @Test
    fun getObjectMapper() {
    }

    @Test
    fun toJson() {
    }

    @Test
    fun toMutableMapSkipEmpty() {
    }

    @Test
    fun toMap() {
    }

    @Test
    fun to() {
    }

    @Test
    fun to1() {
        val expect: List<NameAndValue> = listOf(NameAndValue("a", "1"))
        val toJson = JsonUtil.toJson(expect)
        val actual = JsonUtil.to(toJson, object : TypeReference<List<NameAndValue>>() {})
        Assert.assertEquals(expect.size, actual.size)
        expect.forEachIndexed { index, nameAndValue ->
            Assert.assertEquals(nameAndValue.javaClass, actual[index].javaClass)
            Assert.assertEquals(nameAndValue.key, actual[index].key)
            Assert.assertEquals(nameAndValue.value, actual[index].value)
        }
    }

    @Test
    fun toOrNull() {
    }

    @Test
    fun mapTo() {
    }

    data class NameAndValue(
        val key: String,
        val value: String
    )
}
