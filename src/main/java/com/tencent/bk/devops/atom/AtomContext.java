package com.tencent.bk.devops.atom;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tencent.bk.devops.atom.common.Constants;
import com.tencent.bk.devops.atom.pojo.AtomBaseParam;
import com.tencent.bk.devops.atom.pojo.AtomResult;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * 插件上下文
 *
 * @version 1.0
 */
@SuppressWarnings({"unused"})
public class AtomContext<T extends AtomBaseParam> {

    private final String dataDir;
    private final String inputFile;
    private final String outputFile;
    private final T param;

    private final AtomResult result;

    private static final String ATOM_FILE_ENCODING = "UTF-8";

    private static final Logger logger = LoggerFactory.getLogger(AtomContext.class);

    /**
     * 插件定义的参数类
     *
     * @param paramClazz 参数类
     * @throws IOException 如果环境问题导致读不到参数类
     */
    AtomContext(Class<T> paramClazz) throws IOException {
        String value = System.getenv(Constants.DATA_DIR_ENV);
        if (value == null || value.trim().length() == 0 || !(new File(value)).isDirectory()) {
            value = System.getProperty("user.dir");
        }
        dataDir = value;
        value = System.getenv(Constants.INPUT_FILE_ENV);
        if (value == null || value.trim().length() == 0) {
            value = "input.json";
        }
        inputFile = value;
        value = System.getenv(Constants.OUTPUT_FILE_ENV);
        if (value == null || value.trim().length() == 0) {
            value = "output.json";
        }
        outputFile = value;
        param = readParam(paramClazz);
        result = new AtomResult();
    }

    /**
     * 读取请求参数
     *
     * @return 请求参数
     */
    public T getParam() {
        return param;
    }

    /**
     * 获取敏感信息参数
     *
     * @param filedName 字段名
     * @return 敏感信息参数
     */
    public String getSensitiveConfParam(String filedName) {
        Map<String, String> bkSensitiveConfInfo = param.getBkSensitiveConfInfo();
        if (null != bkSensitiveConfInfo) {
            return bkSensitiveConfInfo.get(filedName);
        } else {
            return null;
        }
    }

    /**
     * 获取结果对象
     *
     * @return 结果对象
     */
    public AtomResult getResult() {
        return result;
    }

    private T readParam(Class<T> paramClazz) throws IOException {
        String json = FileUtils.readFileToString(new File(dataDir + "/" + inputFile), ATOM_FILE_ENCODING);
        return JsonUtil.fromJson(json, paramClazz);
    }

    public Map<String, Object> getAllParameters() throws IOException {
        String json = FileUtils.readFileToString(new File(dataDir + "/" + inputFile), ATOM_FILE_ENCODING);
        return JsonUtil.fromJson(json, new TypeReference<Map<String, Object>>() {
        });
    }

    void persistent() throws IOException {
        String json = JsonUtil.toJson(result);
        FileUtils.write(new File(dataDir + "/" + outputFile), json, ATOM_FILE_ENCODING);
    }
}
