package com.tencent.bk.devops.atom;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tencent.bk.devops.atom.pojo.AtomBaseParam;
import com.tencent.bk.devops.atom.pojo.AtomResult;
import com.tencent.bk.devops.atom.utils.http.SdkUtils;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import com.tencent.bk.devops.plugin.api.impl.SensitiveConfApi;
import com.tencent.bk.devops.plugin.pojo.atom.SensitiveConfResp;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    private static final String ATOM_FILE_ENCODING = "UTF-8";

    private static final Logger logger = LoggerFactory.getLogger(AtomContext.class);

    /**
     * 原子定义的参数类
     *
     * @param paramClazz 参数类
     * @throws IOException 如果环境问题导致读不到参数类
     */
    public AtomContext(Class<T> paramClazz) throws IOException {
        dataDir = SdkUtils.getDataDir();
        inputFile = SdkUtils.getInputFile();
        outputFile = SdkUtils.getOutputFile();
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
     * @param filedName 字段名
     * @return 敏感信息参数
     */
    public String getSensitiveConfParam(String filedName){
        Map<String,String> bkSensitiveConfInfo = param.getBkSensitiveConfInfo();
        if(null != bkSensitiveConfInfo){
            return bkSensitiveConfInfo.get(filedName);
        }else{
            return null;
        }
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
        String json =
            FileUtils.readFileToString(new File(dataDir + "/" + inputFile), ATOM_FILE_ENCODING);
        T param = JsonUtil.fromJson(json, paramClazz);
        String atomCode = param.getAtomCode();
        if (atomCode == null || !atomCode.equals(System.getenv("BK_CI_ATOM_CODE"))) {
            // 本地测试环境不调蓝盾接口设置插件敏感信息
            return param;
        }
        SensitiveConfApi sensitiveConfApi = new SensitiveConfApi();
        List<SensitiveConfResp> sensitiveConfList =
            sensitiveConfApi.getAtomSensitiveConf(atomCode).getData();
        if (sensitiveConfList != null && !sensitiveConfList.isEmpty()) {
            Map<String, String> bkSensitiveConfInfo = new HashMap<>();
            for (SensitiveConfResp sensitiveConfResp : sensitiveConfList) {
                bkSensitiveConfInfo.put(
                    sensitiveConfResp.getFieldName(), sensitiveConfResp.getFieldValue());
            }
            // 设置插件敏感信息
            param.setBkSensitiveConfInfo(bkSensitiveConfInfo);
        }
        return param;
    }

    public Map<String,Object>  getAllParameters() throws IOException {
        String json = FileUtils.readFileToString(new File(dataDir + "/" + inputFile), ATOM_FILE_ENCODING);
        return JsonUtil.fromJson(json, new TypeReference<Map<String, Object>>(){});
    }

    void persistent() throws IOException {
        String json = JsonUtil.toJson(result);
        FileUtils.write(new File(dataDir + "/" + outputFile), json, ATOM_FILE_ENCODING);
    }
}
