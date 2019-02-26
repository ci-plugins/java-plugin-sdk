package com.tencent.bk.devops.atom.api;


import com.google.common.collect.Maps;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BaseApi {

    protected String request(Request request, String errorMessage) throws IOException {
        OkHttpClient httpClient = okHttpClient.newBuilder().build();

        Response response = httpClient.newCall(request).execute();

        assert response.body() != null;
        String responseContent = response.body().string();
        if (!response.isSuccessful()) {
            System.err.println("Fail to request(" + request + ") with code " + response.code()
                    + " , message " + response.message() + " and response" + responseContent);
            throw new RuntimeException(errorMessage);
        }
        return responseContent;
    }


    private OkHttpClient okHttpClient = new okhttp3.OkHttpClient.Builder()
            .connectTimeout(5L, TimeUnit.SECONDS)
            .readTimeout(300 * 5L, TimeUnit.SECONDS) // Set to 15 minutes
            .writeTimeout(60L, TimeUnit.SECONDS)
            .build();

    public Request buildGet(String path, Map<String, String> headers) {
        String url = buildUrl(path);
        return new Request.Builder().url(url).headers(Headers.of(getAllHeaders(headers))).get().build();
    }

    public Request buildGet(String path) {
        return buildGet(path, Maps.newHashMap());
    }

    public Request buildPost(String path) {
        return buildPost(path, Maps.newHashMap());
    }

    public Request buildPost(String path, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "");
        return buildPost(path, requestBody, headers);
    }

    public Request buildPost(String path, RequestBody requestBody, Map<String, String> headers) {
        String url = buildUrl(path);
        return new Request.Builder().url(url).headers(Headers.of(getAllHeaders(headers))).post(requestBody).build();
    }

    public Request buildPut(String path) {
        return buildPut(path, Maps.newHashMap());
    }

    public Request buildPut(String path, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "");
        return buildPut(path, requestBody, headers);
    }

    public Request buildPut(String path, RequestBody requestBody, Map<String, String> headers) {
        String url = buildUrl(path);
        return new Request.Builder().url(url).headers(Headers.of(getAllHeaders(headers))).put(requestBody).build();
    }

    public Request buildDelete(String path, Map<String, String> headers) {
        String url = buildUrl(path);
        return new Request.Builder().url(url).headers(Headers.of(getAllHeaders(headers))).delete().build();
    }

    public RequestBody getJsonRequest(Object data) {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JsonUtil.toJson(data));
    }

    public String encode(String parameter) throws UnsupportedEncodingException {
        return URLEncoder.encode(parameter, "UTF-8");
    }

    private String buildUrl(String path) {
        return SdkEnv.genUrl(path);
    }

    private Map<String, String> getAllHeaders(Map<String, String> headers) {
        headers.putAll(SdkEnv.getSdkHeader());
        return headers;
    }
}
