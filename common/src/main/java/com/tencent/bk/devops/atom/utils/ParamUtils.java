package com.tencent.bk.devops.atom.utils;

/**
 * @version 1.0
 */
public class ParamUtils {

    public static String readParam(String paramName) {
        return System.getenv(paramName);
    }
}
