package com.tencent.bk.devops.atom.utils;

import org.apache.commons.lang3.StringUtils;

public class I18nUtil {

    /**
     * 获取插件执行时语言信息
     *
     * @return 插件执行时语言信息
     */
    public static String getLanguage() {
        String language = System.getenv("BK_CI_LOCALE_LANGUAGE");
        if (StringUtils.isEmpty(language)) {
            language = "zh_CN";
        }
        return language;
    }
}
