package com.tencent.bk.devops.atom.utils;

import kotlin.text.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessageUtil {

    private static final Logger logger = LoggerFactory.getLogger(MessageUtil.class);
    private static final String DEFAULT_BASE_NAME = "i18n/message";

    /**
     * 根据语言环境获取对应的描述信息
     *
     * @param messageCode 消息标识
     * @param language    语言信息
     * @return 描述信息
     */
    public static String getMessageByLocale(String messageCode, String language) {
        return getMessageByLocale(messageCode, language, null, DEFAULT_BASE_NAME, null);
    }

    /**
     * 根据语言环境获取对应的描述信息
     *
     * @param messageCode    消息标识
     * @param language       语言信息
     * @param defaultMessage 默认信息
     * @return 描述信息
     */
    public static String getMessageByLocale(String messageCode, String language, String defaultMessage) {
        return getMessageByLocale(messageCode, language, null, DEFAULT_BASE_NAME, defaultMessage);
    }

    /**
     * 根据语言环境获取对应的描述信息
     *
     * @param messageCode 消息标识
     * @param language    语言信息
     * @param params      替换描述信息占位符的参数数组
     * @return 描述信息
     */
    public static String getMessageByLocale(String messageCode, String language, String[] params) {
        return getMessageByLocale(messageCode, language, params, DEFAULT_BASE_NAME, null);
    }

    /**
     * 根据语言环境获取对应的描述信息
     *
     * @param messageCode    消息标识
     * @param language       语言信息
     * @param params         替换描述信息占位符的参数数组
     * @param defaultMessage 默认信息
     * @return 描述信息
     */
    public static String getMessageByLocale(
        String messageCode,
        String language,
        String[] params,
        String defaultMessage
    ) {
        return getMessageByLocale(messageCode, language, params, DEFAULT_BASE_NAME, defaultMessage);
    }

    /**
     * 根据语言环境获取对应的描述信息
     *
     * @param messageCode    消息标识
     * @param language       语言信息
     * @param params         替换描述信息占位符的参数数组
     * @param baseName       基础资源名称
     * @param defaultMessage 默认信息
     * @return 描述信息
     */
    public static String getMessageByLocale(
        String messageCode,
        String language,
        String[] params,
        String baseName,
        String defaultMessage
    ) {
        // 通过resourceBundle获取对应语言的描述信息
        String message = null;
        try {
            String[] parts = language.split("_");
            Locale localeObj = new Locale(language);
            if (parts.length > 1) {
                localeObj = new Locale(parts[0], parts[1]);
            }
            // 根据locale和baseName生成resourceBundle对象
            ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, localeObj);
            // 通过resourceBundle获取对应语言的描述信息
            message = new String(resourceBundle.getString(messageCode).getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
        } catch (Throwable ignored) {
            logger.warn("Fail to get i18nMessage of messageCode[" + messageCode + "]", ignored);
        }
        if (null != params && null != message) {
            MessageFormat mf = new MessageFormat(message);
            // 根据参数动态替换状态码描述里的占位符
            message = mf.format(params);
        }
        if (message == null) {
            message = defaultMessage;
        }
        return message;
    }
}
