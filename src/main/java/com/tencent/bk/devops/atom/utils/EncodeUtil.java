package com.tencent.bk.devops.atom.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class EncodeUtil {
    //判断汉字/特殊符号的方法,编码在\u0080到\uffff之间的都是汉字或其他特殊符号
    private static Boolean isChineseChar(Character ch) {
        return ch.toString().matches("[\u0080-\uffff]");
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
