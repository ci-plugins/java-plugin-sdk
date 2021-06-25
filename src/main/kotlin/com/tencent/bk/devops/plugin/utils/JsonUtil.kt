package com.tencent.bk.devops.plugin.utils

import com.fasterxml.jackson.annotation.JsonFilter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.ser.FilterProvider
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.collect.Maps
import com.tencent.bk.devops.atom.utils.json.annotation.SkipLogField

object JsonUtil {
    private val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule())
        configure(SerializationFeature.INDENT_OUTPUT, true)
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    }

    private val skipEmptyObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule())
        configure(SerializationFeature.INDENT_OUTPUT, true)
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
        setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    }

    fun getObjectMapper() = objectMapper


    private val jsonMappers: MutableMap<String, ObjectMapper> = Maps.newConcurrentMap()

    /**
     * 序列化时忽略bean中的某些字段,字段需要用注解SkipLogFields包括
     *
     * @param bean 对象
     * @param <T>  对象类型
     * @return Json字符串
     * @see SkipLogField
    </T> */
    fun <T : Any> skipLogFields(bean: T): String? {
        return jsonMappers.computeIfAbsent("__skipLogFields__" + bean.javaClass.name) { s: String? ->
            val nonEmptyMapper = ObjectMapper()
            var aClass: Class<*>? = bean.javaClass
            val skipFields: MutableSet<String> = HashSet()
            while (aClass != null) {
                val fields = aClass.declaredFields
                for (field in fields) {
                    val fieldAnnotation = field.getAnnotation(SkipLogField::class.java) ?: continue
                    if (fieldAnnotation.value.trim().isNotEmpty()) {
                        skipFields.add(fieldAnnotation.value)
                    } else {
                        skipFields.add(field.name)
                    }
                }
                aClass = aClass.superclass
            }
            if (skipFields.isNotEmpty()) {
                nonEmptyMapper.addMixIn(bean.javaClass, SkipLogField::class.java)
                // 仅包含
                val filterProvider: FilterProvider = SimpleFilterProvider()
                    .addFilter(SkipLogField::class.java.getAnnotation(JsonFilter::class.java).value,
                        SimpleBeanPropertyFilter.serializeAllExcept(skipFields))
                nonEmptyMapper.setFilterProvider(filterProvider)
            }
            nonEmptyMapper
        }.writeValueAsString(bean)
    }
    /**
     * 转成Json
     */
    fun toJson(bean: Any): String {
        if (ReflectUtil.isNativeType(bean) || bean is String) {
            return bean.toString()
        }
        return getObjectMapper().writeValueAsString(bean)!!
    }

    /**
     * 将对象转可修改的Map,
     * 注意：会忽略掉值为空串和null的属性
     */
    fun toMutableMapSkipEmpty(bean: Any): MutableMap<String, Any> {
        if (ReflectUtil.isNativeType(bean)) {
            return mutableMapOf()
        }
        return if (bean is String)
            skipEmptyObjectMapper.readValue<MutableMap<String, Any>>(
                bean.toString(),
                object : TypeReference<MutableMap<String, Any>>() {})
        else
            skipEmptyObjectMapper.readValue<MutableMap<String, Any>>(
                skipEmptyObjectMapper.writeValueAsString(bean),
                object : TypeReference<MutableMap<String, Any>>() {})
    }

    /**
     * 将对象转不可修改的Map
     * 注意：会忽略掉值为null的属性
     */
    fun toMap(bean: Any): Map<String, Any> {
        return when {
            ReflectUtil.isNativeType(bean) -> mapOf()
            bean is String -> to(bean)
            else -> to(getObjectMapper().writeValueAsString(bean))
        }
    }

    /**
     * 将json转指定类型对象
     * @param json json字符串
     * @return 指定对象
     */
    fun <T> to(json: String): T {
        return getObjectMapper().readValue<T>(json, object : TypeReference<T>() {})
    }

    fun <T> to(json: String, typeReference: TypeReference<T>): T {
        return getObjectMapper().readValue<T>(json, typeReference)
    }

    fun <T> to(json: String, type: Class<T>): T = getObjectMapper().readValue(json, type)

    fun <T> toOrNull(json: String?, type: Class<T>): T? {
        return if (json.isNullOrBlank()) {
            null
        } else {
            getObjectMapper().readValue(json, type)
        }
    }

    fun <T> mapTo(map: Map<String, Any>, type: Class<T>): T = getObjectMapper().readValue(
        getObjectMapper().writeValueAsString(map), type)
}
