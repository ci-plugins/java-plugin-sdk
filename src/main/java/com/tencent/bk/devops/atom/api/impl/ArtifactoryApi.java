package com.tencent.bk.devops.atom.api.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tencent.bk.devops.atom.api.BaseApi;
import com.tencent.bk.devops.atom.pojo.Result;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ArtifactoryApi extends BaseApi {

    private final static Logger logger = LoggerFactory.getLogger(ArtifactoryApi.class);

    @SuppressWarnings("all")
    public  Result<List<String>> getArtifactoryFileUrl(String projectCode,String pipelineId,String buildId,String artifactoryType,String path){
        StringBuilder urlBuilder = new StringBuilder("/artifactory/api/build/artifactories/thirdPartyDownloadUrl?artifactoryType=");
        urlBuilder.append(artifactoryType).append("&path=/").append(path).append("&ttl=").append(3600);
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

}
