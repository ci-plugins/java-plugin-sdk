package com.tencent.bk.devops.atom.api.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tencent.bk.devops.atom.api.BaseApi;
import com.tencent.bk.devops.atom.pojo.Result;
import com.tencent.bk.devops.atom.pojo.image.PushImageRequest;
import com.tencent.bk.devops.atom.pojo.image.PushImageTask;
import com.tencent.bk.devops.atom.utils.http.SdkUtils;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ImageApi extends BaseApi {

    private final static Logger logger = LoggerFactory.getLogger(ImageApi.class);

    /**
     * 推送镜像
     * @param pushImageRequest 推送镜像请求报文体
     * @return 推送镜像结果
     */
    @SuppressWarnings("all")
    public Result<PushImageTask> pushImageTask(PushImageRequest pushImageRequest){

        String path = "/image/api/build/image/common/push";
        Map<String, String> parmMap = JsonUtil.fromJson(JsonUtil.toJson(pushImageRequest), Map.class);
        String inputJson = null;
        try {
            inputJson = FileUtils.readFileToString(new File(SdkUtils.getDataDir() + "/" + SdkUtils.getInputFile()), Charset.defaultCharset());
        } catch (IOException e) {
            logger.error("parse inputJson throw Exception", e);
        }
        Map<String, String> inputMap = JsonUtil.fromJson(inputJson, Map.class);
        if (inputMap == null){
            logger.info("input loading error");
            return new Result(null);
        }
        parmMap.put("projectId", inputMap.get("project.name"));
        parmMap.put("buildId", inputMap.get("pipeline.build.id"));
        parmMap.put("pipelineId", inputMap.get("pipeline.id"));
        RequestBody requestBody = RequestBody.create(JSON_CONTENT_TYPE, JsonUtil.toJson(parmMap));
        Request request = buildPost(path, requestBody, new HashMap<String, String>());
        String responseContent = null;
        try {
            responseContent = super.request(request,"推送镜像失败");
        } catch (IOException e) {
            logger.error("pushImageTask throw Exception", e);
        }
        if(null != responseContent){
            return JsonUtil.fromJson(responseContent,new TypeReference<Result<PushImageTask>>(){});
        }else{
            return new Result(null);
        }
    }

    /**
     * 查询推送镜像结果
     * @param userId 操作者
     * @param taskId 推送任务ID
     * @return 推送镜像结果
     */
    @SuppressWarnings("all")
    public Result<PushImageTask> queryImageTask(String userId, String taskId){
        StringBuilder pathBuilder = new StringBuilder("/image/api/build/image/common/query?userId=");
        pathBuilder.append(userId).append("&taskId=").append(taskId);
        Request request = super.buildGet(pathBuilder.toString());
        String responseContent = null;
        try {
            responseContent = super.request(request,"查询推送镜像结果信息失败");
        } catch (IOException e) {
            logger.error("queryImageTask throw Exception", e);
        }
        if(null != responseContent){
            return JsonUtil.fromJson(responseContent,new TypeReference<Result<PushImageTask>>(){});
        }else{
            return new Result(null);
        }
    }

}
