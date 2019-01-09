package com.tencent.bk.devops.atom;

import com.tencent.bk.devops.atom.common.Constants;
import com.tencent.bk.devops.atom.exception.AtomException;
import com.tencent.bk.devops.atom.pojo.AtomBaseParam;
import com.tencent.bk.devops.atom.pojo.AtomResult;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;


/**
 * 原子上下文
 *
 * @version 1.0
 */
@SuppressWarnings({"unused"})
public class AtomContext<T extends AtomBaseParam> {

    private final String dataDir;
    private final String inputFile;
    private final String outputFile;
    private T param;

    private AtomResult result;

    /**
     * 原子定义的参数类
     *
     * @param paramClazz 参数类
     * @throws IOException  如果环境问题导致读不到参数类
     */
    AtomContext(Class<T> paramClazz) throws IOException {
        dataDir = readEnv(Constants.DATA_DIR_ENV);
        inputFile = readEnv(Constants.INPUT_FILE_ENV);
        outputFile = readEnv(Constants.OUTPUT_FILE_ENV);
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
     * 获取结果对象
     *
     * @return 结果对象
     */
    @SuppressWarnings({"all"})
    public AtomResult getResult() {
        return result;
    }

    private T readParam(Class<T> paramClazz) throws IOException {
        String json = FileUtils.readFileToString(new File(dataDir + "/" + inputFile), Charset.defaultCharset());
        return JsonUtil.fromJson(json, paramClazz);
    }

    private String readEnv(String envName) {
        String value = System.getenv(envName);
        if (StringUtils.isBlank(value)) {
            System.err.printf("environment [%s] is empty", envName);
            throw new AtomException(String.format("environment [%s] is empty", envName));
        }
        return value;
    }

    void persistent() throws IOException {
        String json = JsonUtil.toJson(result);
        FileUtils.write(new File(dataDir + "/" + outputFile), json, Charset.defaultCharset());
    }
}
