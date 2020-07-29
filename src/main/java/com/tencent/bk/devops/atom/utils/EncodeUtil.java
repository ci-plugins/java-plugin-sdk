package com.tencent.bk.devops.atom.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class EncodeUtil {
    //判断汉字的方法,只要编码在\u4e00到\u9fa5之间的都是汉字
    private static Boolean isChineseChar(Character ch) {
        return ch.toString().matches("[\u4e00-\u9fa5]");
    }

    public static String encodeChinese(String str) {
        StringBuilder sb = new StringBuilder();
        //遍历字符串
        for (Character ch : str.toCharArray()) {
            if (isChineseChar(ch)) {
                try {
                    sb.append(URLEncoder.encode(ch + "", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
