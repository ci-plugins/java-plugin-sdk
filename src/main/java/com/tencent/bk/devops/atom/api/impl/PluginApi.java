package com.tencent.bk.devops.atom.api.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tencent.bk.devops.atom.api.BaseApi;
import com.tencent.bk.devops.atom.pojo.Result;
import com.tencent.bk.devops.atom.pojo.artifactory.OnsHostInfo;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PluginApi extends BaseApi {

    private static final Logger logger = LoggerFactory.getLogger(PluginApi.class);

    /**
     * 获取ons名字信息
     * @param domainName 域名
     * @return
     */
    @SuppressWarnings("all")
    public Result<OnsHostInfo> getOnsHostInfo(String domainName){
        Request request = super.buildGet("/plugin/api/build/ons/host/domains/" + domainName);
        String responseContent = null;
        try {
            responseContent = super.request(request,"获取请求IP地址信息失败");
        } catch (IOException e) {
            logger.error("getOnsHostInfo throw Exception", e);
        }
        logger.info("getOnsHostInfo responseContent is:{}", responseContent);
        if(null != responseContent){
            return JsonUtil.fromJson(responseContent,new TypeReference<Result<OnsHostInfo>>(){});
        }else{
            return new Result(null);
        }
    }

}
