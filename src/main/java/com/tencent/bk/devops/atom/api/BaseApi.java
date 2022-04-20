package com.tencent.bk.devops.atom.api;


import com.google.common.collect.Maps;
import com.tencent.bk.devops.atom.exception.RemoteServiceException;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpRetryException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BaseApi {

    protected static final MediaType JSON_CONTENT_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final Logger logger = LoggerFactory.getLogger(BaseApi.class);
    private static final long sleepTimeMills = 500L;
    private static final int DEFAULT_RETRY_TIME = 5;

    protected Response requestForResponse(Request request, int retryCount) throws IOException, InterruptedException {
        OkHttpClient httpClient = okHttpClient.newBuilder().build();
        boolean retryFlag;
        try {
            Response response = httpClient.newCall(request).execute();
            if (Arrays.asList(502, 503, 504).contains(response.code())) {
                retryFlag = true;
            } else {
                return response;
            }
        } catch (UnknownHostException e) {
            logger.warn("UnknownHostException|request({}),error is :{}, try to retry {}",
                    request, e.getMessage(), retryCount
            );
            retryFlag = retryCount > 0;
        } catch (ConnectException e) {
            logger.warn(
                    "ConnectException|request({}),error is :{}, try to retry {}",
                    request, e.getMessage(), retryCount
            );
            retryFlag = retryCount > 0;
        } catch (SocketTimeoutException re) {
            if ("connect timed out".equals(re.getMessage()) ||
                    ("GET".equals(request.method()) && "timeout".equals(re.getMessage()))
            ) {
                logger.warn("SocketTimeoutException|request({}),error is :{}, try to retry {}",
                        request, re.getMessage(), retryCount
                );
                retryFlag = retryCount > 0;
            } else {
                logger.error("Fail to request({})", request, re);
                throw re;
            }
        } catch (Exception error) {
            logger.error("Fail to request({})", request, error);
            throw new RemoteServiceException(
                    String.format("Fail to request(%s),error is:%s", request, error.getMessage()),
                    500, ""
            );
        }
        if (retryFlag && retryCount > 0) {
            logger.warn(
                    "Fail to request({}), retry after {} ms", request, sleepTimeMills
            );
            Thread.sleep(sleepTimeMills);
            return requestForResponse(request, retryCount - 1);
        } else {
            logger.error("Fail to request({}), try to retry {}", request, retryCount);
            throw new HttpRetryException(
                    String.format("Fail to request(%s), try to retry %s", request, retryCount),
                    999
            );
        }
    }

    protected String retryRequest(Request request, String errorMessage, int retryCount) throws IOException {
        try (Response response = requestForResponse(request, retryCount)) {
            String responseContent = response.body() != null ? response.body().string() : null;
            if (!response.isSuccessful()) {
                logger.error("Fail to request(" + request + ") with code " + response.code()
                        + " , message " + response.message() + " and response" + responseContent);
                logger.info("excep>>>>>>>>>>>>" + response);
                throw new RemoteServiceException(errorMessage, response.code(), responseContent);
            }
            return responseContent;
        } catch (InterruptedException e) {
            throw new RemoteServiceException(errorMessage, 500, e.getMessage());
        }
    }

    protected String retryRequest(Request request, String errorMessage) throws IOException {
        return retryRequest(request, errorMessage, DEFAULT_RETRY_TIME);
    }

    protected String request(Request request, String errorMessage) throws IOException {
        OkHttpClient httpClient = okHttpClient.newBuilder().build();
        try (Response response = httpClient.newCall(request).execute()) {
            String responseContent = response.body() != null ? response.body().string() : null;
            if (!response.isSuccessful()) {
                logger.error("Fail to request(" + request + ") with code " + response.code()
                    + " , message " + response.message() + " and response" + responseContent);
                logger.info("excep>>>>>>>>>>>>" + response);
                throw new RemoteServiceException(errorMessage, response.code(), responseContent);
            }
            return responseContent;
        }
    }

    protected OkHttpClient okHttpClient = new okhttp3.OkHttpClient.Builder()
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

    public Request buildDelete(String path, RequestBody requestBody, Map<String, String> headers) {
        String url = buildUrl(path);
        return new Request.Builder().url(url).headers(Headers.of(getAllHeaders(headers))).delete(requestBody).build();
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
