package com.tencent.bk.devops.atom.api;

import com.google.common.collect.Maps;
import com.tencent.bk.devops.atom.common.Constants;
import com.tencent.bk.devops.atom.utils.http.SdkUtils;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @version 1.0 */
@Setter
@Getter
public class SdkEnv {

    private static final Logger logger = LoggerFactory.getLogger(SdkEnv.class);

    private String buildType;
    private String projectId;
    private String agentId;
    private String secretKey;
    private String gateway;
    private String buildId;
    private String vmSeqId;
    private String fileGateway;
    private String taskId;

    private static SdkEnv instance;

    private static final String HTTP_PROTOCOL = "http://";

    private static final String HTTPS_PROTOCOL = "https://";

    public static Map<String, String> getSdkHeader() {
        Map<String, String> map = Maps.newHashMap();
        map.put(Header.AUTH_HEADER_DEVOPS_BUILD_TYPE, instance.buildType);

        map.put(Header.AUTH_HEADER_DEVOPS_PROJECT_ID, instance.projectId);
        map.put(Header.AUTH_HEADER_PROJECT_ID, instance.projectId);

        map.put(Header.AUTH_HEADER_DEVOPS_AGENT_SECRET_KEY, instance.secretKey);

        map.put(Header.AUTH_HEADER_DEVOPS_AGENT_ID, instance.agentId);

        map.put(Header.AUTH_HEADER_DEVOPS_VM_SEQ_ID, instance.vmSeqId);

        map.put(Header.AUTH_HEADER_DEVOPS_BUILD_ID, instance.buildId);
        map.put(Header.AUTH_HEADER_BUILD_ID, instance.buildId);

        map.put(Header.AUTH_HEADER_DEVOPS_TASK_ID, instance.taskId);
        return map;
    }

    @SuppressWarnings("all")
    public static void init() throws IOException {
        String dataDir = System.getenv(Constants.DATA_DIR_ENV);
        if (dataDir == null || dataDir.trim().length() == 0 || !(new File(dataDir)).isDirectory()) {
            dataDir = System.getProperty("user.dir");
        }
        String sdkFile = ".sdk.json";
        File file = new File(dataDir + "/" + sdkFile);
        try {
            String json = FileUtils.readFileToString(file, Charset.defaultCharset());
            instance = JsonUtil.fromJson(json, SdkEnv.class);
        } finally {
            boolean flag = file.delete(); // 读取完后删除文件
            logger.info("[java-atom-sdk] delete sdkFile result is:{}", flag);
        }
    }

    public static String genUrl(String path) {
        String newPath = path.trim();
        if (newPath.startsWith(HTTP_PROTOCOL) || newPath.startsWith(HTTPS_PROTOCOL)) {
            return newPath;
        } else {
            String urlPrefix = instance.gateway;
            if (!SdkUtils.hasProtocol(instance.gateway)) {
                urlPrefix = HTTP_PROTOCOL + instance.gateway;
            }
            return urlPrefix + "/" + StringUtils.removeStart(newPath, "/");
        }
    }

    public static String getGatewayHost() {
        String host = instance.gateway;
        if (host.startsWith(HTTP_PROTOCOL)) {
            host = host.replace(HTTP_PROTOCOL, "");
        } else if (host.startsWith(HTTPS_PROTOCOL)) {
            host = host.replace(HTTPS_PROTOCOL, "");
        }
        return host;
    }

    public static String getVmSeqId() {
        return instance.vmSeqId;
    }

    public static String getFileGateway() {
        return instance.fileGateway;
    }
}
