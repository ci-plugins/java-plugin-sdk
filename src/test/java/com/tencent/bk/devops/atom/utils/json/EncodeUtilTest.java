package com.tencent.bk.devops.atom.utils.json;

import com.google.common.collect.Lists;
import com.tencent.bk.devops.atom.utils.EncodeUtil;
import com.tencent.bk.devops.atom.utils.json.annotation.SkipLogField;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @version 1.0
 */
public class EncodeUtilTest {

    @Before
    public void setup() {
    }

    @Test
    public void encodeChinese() {
        String strWithoutChinese = "http://www.tencent.com";
        String strWithChinese = "http://www.tencent.com/我是中文/abc";
        Assert.assertEquals(strWithoutChinese, EncodeUtil.encodeChinese(strWithoutChinese));
        Assert.assertNotEquals(strWithChinese, EncodeUtil.encodeChinese(strWithChinese));
        Assert.assertTrue(EncodeUtil.encodeChinese(strWithChinese).endsWith("/abc"));
        String strWithSpecialChar = "【主干】SProject_SOC_Build";
        String strWithHttpSchema = "http://xxx.com/【主干】?token=xxx";
        Assert.assertNotEquals(strWithSpecialChar, EncodeUtil.encodeChinese(strWithSpecialChar));
        Assert.assertNotEquals(strWithHttpSchema, EncodeUtil.encodeChinese(strWithHttpSchema));
        Assert.assertTrue(EncodeUtil.encodeChinese(strWithHttpSchema).contains("http://"));
        Assert.assertTrue(EncodeUtil.encodeChinese(strWithHttpSchema).contains("?"));
        Assert.assertFalse(EncodeUtil.encodeChinese(strWithHttpSchema).contains("【"));
        Assert.assertFalse(EncodeUtil.encodeChinese(strWithHttpSchema).contains("】"));
    }

}
