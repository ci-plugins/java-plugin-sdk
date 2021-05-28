package com.tencent.bk.devops.plugin.utils

import org.junit.Assert
import org.junit.Before
import org.junit.Test


/**
 * @version 1.0
 */
class EncodeUtilTest {
    @Before
    fun setup() {
    }

    @Test
    fun encodeChinese() {
        val strWithoutChinese = "http://www.tencent.com"
        val strWithChinese = "http://www.tencent.com/我是中文/abc"
        Assert.assertEquals(strWithoutChinese, EncodeUtil.encodeChinese(strWithoutChinese))
        Assert.assertNotEquals(strWithChinese, EncodeUtil.encodeChinese(strWithChinese))
        Assert.assertTrue(EncodeUtil.encodeChinese(strWithChinese).endsWith("/abc"))
        val strWithSpecialChar = "【主干】SProject_SOC_Build"
        val strWithHttpSchema = "http://xxx.com/【主干】?token=xxx"
        Assert.assertNotEquals(strWithSpecialChar, EncodeUtil.encodeChinese(strWithSpecialChar))
        Assert.assertNotEquals(strWithHttpSchema, EncodeUtil.encodeChinese(strWithHttpSchema))
        Assert.assertTrue(EncodeUtil.encodeChinese(strWithHttpSchema).contains("http://"))
        Assert.assertTrue(EncodeUtil.encodeChinese(strWithHttpSchema).contains("?"))
        Assert.assertFalse(EncodeUtil.encodeChinese(strWithHttpSchema).contains("【"))
        Assert.assertFalse(EncodeUtil.encodeChinese(strWithHttpSchema).contains("】"))
    }
}

