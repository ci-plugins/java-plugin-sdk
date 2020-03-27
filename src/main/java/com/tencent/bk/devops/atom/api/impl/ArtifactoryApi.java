package com.tencent.bk.devops.atom.api.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tencent.bk.devops.atom.api.BaseApi;
import com.tencent.bk.devops.atom.api.SdkEnv;
import com.tencent.bk.devops.atom.pojo.Result;
import com.tencent.bk.devops.atom.pojo.artifactory.FileDetail;
import com.tencent.bk.devops.atom.utils.http.SdkUtils;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本API作废，请使用新的API
 *
 * @see com.tencent.bk.devops.plugin.api.impl.ArtifactoryApi
 */
@Deprecated
public class ArtifactoryApi extends BaseApi {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactoryApi.class);

    /**
     * 获取构建文件下载路径
     *
     * @param artifactoryType 版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path            路径 多路径使用","或者";"分割
     * @return 文件下载路径数组
     */
    @SuppressWarnings("all")
    public Result<List<String>> getArtifactoryFileUrl(String artifactoryType, String path) {
        StringBuilder urlBuilder = new StringBuilder("/artifactory/api/build/artifactories/thirdPartyDownloadUrl?artifactoryType=");
        urlBuilder.append(artifactoryType).append("&path=").append(path).append("&ttl=").append(3600); //下载链接有效期设定为1小时
        String requestUrl = urlBuilder.toString();
        logger.info("the requestUrl is:{}", requestUrl);
        Request request = super.buildGet(urlBuilder.toString());
        String responseContent = null;
        try {
            responseContent = super.request(request, "获取包路径失败");
        } catch (IOException e) {
            logger.error("get artifactoryFileUrl throw Exception", e);
        }
        if (null != responseContent) {
            Result<List<String>> srcUrlResult = JsonUtil.fromJson(responseContent, new TypeReference<Result<List<String>>>() {
            });
            if (srcUrlResult == null) {
                logger.info("no ArtifactoryFileUrl is found");
                return new Result(null);
            }
            List<String> srcUrlList = srcUrlResult.getData();
            List<String> finalSrcUrlList = new ArrayList<String>();
            for (String srcUrl : srcUrlList) {
                srcUrl = srcUrl.replace(getUrlHost(srcUrl), SdkEnv.getGatewayHost());
                finalSrcUrlList.add(srcUrl);
            }
            logger.info("getArtifactoryFileUrl responseContent is:{}", JsonUtil.toJson(finalSrcUrlList));
            return new Result(finalSrcUrlList);
        } else {
            logger.info("getArtifactoryFileUrl responseContent is null");
            return new Result(null);
        }
    }

    /**
     * 下载构建文件到本地
     *
     * @param artifactoryType 版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path            路径
     * @return 文件在本地的保存路径
     */
    @SuppressWarnings("all")
    public Result<List<String>> downloadArtifactoryFileToLocal(String artifactoryType, String path) {
        return downloadArtifactoryFileToLocal(artifactoryType, path, SdkUtils.getDataDir());
    }

    /**
     * 下载构建文件到本地指定目录
     *
     * @param artifactoryType 版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path            路径
     * @param saveDir         文件保存目录
     * @return 文件在本地的保存路径
     */
    @SuppressWarnings("all")
    public Result<List<String>> downloadArtifactoryFileToLocal(String artifactoryType, String path, String saveDir) {
        Result<List<String>> getFileUrlResult = getArtifactoryFileUrl(artifactoryType, path);
        logger.info("the getFileUrlResult is:{}", JsonUtil.toJson(getFileUrlResult));
        List<String> srcUrlList = null;
        if (0 != getFileUrlResult.getStatus()) {
            return new Result(getFileUrlResult.getStatus(), getFileUrlResult.getMessage());
        } else {
            srcUrlList = getFileUrlResult.getData();
        }
        List<String> saveFilePathList = new ArrayList<String>();
        if (null != srcUrlList) {
            File saveDirFile = new File(saveDir);
            if (!saveDirFile.exists()) {
                boolean flag = saveDirFile.mkdirs();
                logger.info("create dir result is:{}", flag);
                if (flag) {
                    return new Result(saveFilePathList);
                }
            }
            for (String srcUrl : srcUrlList) {
                String[] arrays = srcUrl.split("/");
                String lastItem = arrays[arrays.length - 1];
                String fileName = lastItem.substring(0, lastItem.indexOf("?"));
                InputStream inputStream = null;
                OutputStream outputStream = null;
                String saveFilePath = null;
                try {
                    URL netUrl = new URL(srcUrl);
                    HttpURLConnection conn = (HttpURLConnection) netUrl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5 * 1000);
                    inputStream = conn.getInputStream();
                    saveFilePath = saveDir + "/" + fileName;
                    logger.info("the saveFilePath is:{}", saveFilePath);
                    outputStream = new FileOutputStream(saveFilePath); //把文件下载到指定目录下面
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    saveFilePathList.add(saveFilePath);
                    logger.info("the saveFilePathList is:{}", JsonUtil.toJson(saveFilePathList));
                } catch (IOException e) {
                    logger.error("downloadArtifactoryFileToLocal error!", e);
                } finally {
                    if (null != outputStream) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            logger.error("outputStream close error!", e);
                        } finally {
                            if (null != inputStream) {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    logger.error("inputStream close error!", e);
                                }
                            }
                        }
                    }
                }
            }
        }
        logger.info("downloadArtifactoryFileToLocal saveFilePathList is:{}", JsonUtil.toJson(saveFilePathList));
        return new Result(saveFilePathList);
    }

    /**
     * 获取仓库的构件元数据
     *
     * @param artifactoryType 版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path            文件路径 多路径使用","或者";"分割
     * @return 文件下载路径数组
     */
    @SuppressWarnings("all")
    public Result<List<FileDetail>> getArtifactsProperties(String artifactoryType, String path) {
        StringBuilder urlBuilder = new StringBuilder("/artifactory/api/build/artifactories/getPropertiesByRegex?artifactoryType=");
        urlBuilder.append(artifactoryType).append("&path=").append(path);
        String requestUrl = urlBuilder.toString();
        logger.info("the requestUrl is:{}", requestUrl);
        Request request = super.buildGet(urlBuilder.toString());
        String responseContent = null;
        try {
            responseContent = super.request(request, "获取包路径失败");
        } catch (IOException e) {
            logger.error("get artifactoryProperties throw Exception", e);
        }
        if (null != responseContent) {
            Result<List<FileDetail>> srcUrlResult = JsonUtil.fromJson(responseContent, new TypeReference<Result<List<FileDetail>>>() {
            });
            if (srcUrlResult == null) {
                logger.info("no ArtifactoryFileUrl is found");
                return new Result(null);
            }
            List<FileDetail> fileDetailList = srcUrlResult.getData();
            logger.info("getArtifactoryProperties responseContent is:{}", JsonUtil.toJson(fileDetailList));
            return new Result(fileDetailList);
        } else {
            logger.info("getArtifactoryProperties responseContent is null");
            return new Result(null);
        }
    }

    private String getUrlHost(String url) {
        Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+(:[0-9]{1,5})?");
        Matcher matcher = p.matcher(url);
        String host = null;
        if (matcher.find()) {
            host = matcher.group();
        }
        return host;
    }
}
