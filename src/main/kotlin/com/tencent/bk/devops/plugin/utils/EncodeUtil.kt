package com.tencent.bk.devops.plugin.utils

import java.net.URLEncoder

/**
 *
 * @version 1.0
 */
object EncodeUtil {
    //判断汉字的方法,只要编码在\u4e00到\u9fa5之间的都是汉字
    fun isChineseChar(ch: Char): Boolean {
        return ch.toString().matches(Regex("[\u4e00-\u9fa5]"))
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
