package com.tencent.bk.devops.atom.api.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tencent.bk.devops.atom.api.BaseApi;
import com.tencent.bk.devops.atom.pojo.Result;
import com.tencent.bk.devops.atom.utils.http.SdkUtils;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ArtifactoryApi extends BaseApi {

    private final static Logger logger = LoggerFactory.getLogger(ArtifactoryApi.class);

    /**
     * 获取构建文件下载路径
     * @param artifactoryType  版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path 路径 多路径使用","或者";"分割
     * @return 文件下载路径数组
     */
    @SuppressWarnings("all")
    public  Result<List<String>> getArtifactoryFileUrl(String artifactoryType,String path){
        StringBuilder urlBuilder = new StringBuilder("/artifactory/api/build/artifactories/thirdPartyDownloadUrl?artifactoryType=");
        urlBuilder.append(artifactoryType).append("&path=").append(path).append("&ttl=").append(3600); //下载链接有效期设定为1小时
        String requestUrl = urlBuilder.toString();
        logger.info("the requestUrl is:{}",requestUrl);
        Request request = super.buildGet(urlBuilder.toString());
        String responseContent = null;
        try {
            responseContent = super.request(request,"获取包路径失败");
        } catch (IOException e) {
            logger.error("get artifactoryFileUrl throw Exception", e);
        }
        logger.info("getArtifactoryFileUrl responseContent is:{}", responseContent);
        if(null != responseContent){
            return JsonUtil.fromJson(responseContent,new TypeReference<Result<List<String>>>(){});
        }else{
            return new Result(null);
        }
    }

    /**
     * 下载构建文件到本地
     * @param artifactoryType  版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path 路径
     * @return 文件在本地的保存路径
     */
    @SuppressWarnings("all")
    public  Result<String>  downloadArtifactoryFileToLocal(String artifactoryType,String path){
        Result<List<String>> getFileUrlResult = getArtifactoryFileUrl(artifactoryType,path);
        logger.info("the getFileUrlResult is:{}", JsonUtil.toJson(getFileUrlResult));
        String srcUrl = null;
        if (0 != getFileUrlResult.getStatus()) {
            return new Result(getFileUrlResult.getStatus(),getFileUrlResult.getMessage());
        } else {
            List<String> data = getFileUrlResult.getData();
            if (null != data && data.size() > 0) {
                srcUrl = data.get(0);
            }
        }
        String[] arrays = path.split("/");
        String fileName = arrays[arrays.length-1];
        int index = fileName.indexOf("?");
        if(-1!=index){
            fileName = fileName.substring(0,index);
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        String saveFilePath = null;
        try {
            URL netUrl = new URL(srcUrl);
            HttpURLConnection conn = (HttpURLConnection) netUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5*1000);
            inputStream = conn.getInputStream();
            String dataDir = SdkUtils.getDataDir();
            logger.info("the dataDir is:{}", dataDir);
            saveFilePath = dataDir+"/"+fileName;
            outputStream = new FileOutputStream(saveFilePath); //把文件下载到dataDir目录下面
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,len);
            }
        } catch (IOException e) {
            logger.error("downloadArtifactoryFileToLocal error!", e);
        } finally {
            if(null != outputStream){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("outputStream close error!", e);
                }finally {
                    if(null != inputStream){
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            logger.error("inputStream close error!", e);
                        }
                    }
                }
            }
        }
        return new Result(saveFilePath);
    }


}
