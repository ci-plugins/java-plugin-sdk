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
    }

}
