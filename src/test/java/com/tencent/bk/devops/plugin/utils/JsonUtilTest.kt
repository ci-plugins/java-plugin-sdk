package com.tencent.bk.devops.plugin.utils

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.utils.json.annotation.SkipLogField
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

    @Test
    fun isBoolean() {
        val p = IsBoolean(isHelmChartEnabled = true, offlined = true, isSecrecy = true, exactResource = 999)
        val allJson = JsonUtil.toJson(p)

        println("正常的Json序列化不受影响=$allJson")

        val allFieldsMap = JsonUtil.to<Map<String, Any>>(allJson)
        // 所有字段都存在，否则就是有问题
        Assert.assertNotNull(allFieldsMap["is_helm_chart_enabled"])
        Assert.assertNotNull(allFieldsMap["is_offlined"])
        Assert.assertNotNull(allFieldsMap["is_secrecy"])
        Assert.assertNotNull(allFieldsMap["is_exact_resource"])

        println(JsonUtil.to(allJson, IsBoolean::class.java))

        val skipLogFieldJson = JsonUtil.skipLogFields(p)

        Assert.assertNotNull(skipLogFieldJson)
        println("脱密后的Json不会有skipLogField的敏感信息=$skipLogFieldJson")

        val haveNoSkipLogFieldsMap = JsonUtil.to<Map<String, Any>>(skipLogFieldJson!!)
        // 以下字段受SkipLogField注解影响，是不会出现的，如果有则说明有问题
        Assert.assertNull(haveNoSkipLogFieldsMap["is_helm_chart_enabled"])
        // 未受SkipLogField注解影响的字段是存在的
        Assert.assertNotNull(haveNoSkipLogFieldsMap["is_offlined"])
        Assert.assertNotNull(haveNoSkipLogFieldsMap["is_secrecy"])
        Assert.assertNotNull(haveNoSkipLogFieldsMap["is_exact_resource"])
    }

    data class NameAndValue(
        val key: String,
        @SkipLogField
        val value: String
    )

    data class IsBoolean(
        @SkipLogField("is_helm_chart_enabled")
        @get:JsonProperty("is_helm_chart_enabled")
        val isHelmChartEnabled: Boolean,
        @get:JsonProperty("is_offlined")
        @set:JsonProperty("is_offlined")
        var offlined: Boolean?,
        @get:JsonProperty("is_secrecy")
        @set:JsonProperty("is_secrecy")
        var isSecrecy: Boolean?,
        @get:JsonProperty("is_exact_resource")
        val exactResource: Int = 1
    )
}
