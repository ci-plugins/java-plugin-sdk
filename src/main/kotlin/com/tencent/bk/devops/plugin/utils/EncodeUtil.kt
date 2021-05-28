package com.tencent.bk.devops.plugin.utils

import java.net.URLEncoder

/**
 *
 * @version 1.0
 */
object EncodeUtil {
    //判断汉字的方法,只要编码在\u0080到\uFFFF之间的都是汉字或特殊字符
    fun isChineseChar(ch: Char): Boolean {
        return ch.toString().matches(Regex("[\u0080-\uFFFF]"))
    }

    fun encodeChinese(str: String): String {
        val sb = java.lang.StringBuilder()
        //遍历字符串
        for (ch in str.toCharArray()) {
            if (isChineseChar(ch)) {
                sb.append(URLEncoder.encode(ch + "", "UTF-8"))
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
