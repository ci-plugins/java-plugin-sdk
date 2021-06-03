package com.tencent.bk.devops.atom.utils.http;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * http请求工具类
 */
public class OkHttpUtils {

    private static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    private static final Logger logger = LoggerFactory.getLogger(OkHttpUtils.class);

    private static long finalConnectTimeout = 5L;
    private static long finalWriteTimeout = 60L;
    private static long finalReadTimeout = 60L;

    private static final long finalLongConnectTimeout = 30L;
    private static final long finalLongWriteTimeout = 60L * 30;
    private static final long finalLongReadTimeout = 60L * 30;

    private static OkHttpClient createClient(long connectTimeout, long writeTimeout, long readTimeout) {
        return createRetryOptionClient(connectTimeout, writeTimeout, readTimeout, true);
    }

    private static OkHttpClient createLongClient() {
        return createRetryOptionClient(finalLongConnectTimeout, finalLongWriteTimeout, finalLongReadTimeout, true);
    }

    private static OkHttpClient createRetryOptionClient(
        long connectTimeout,
        long writeTimeout,
        long readTimeout,
        boolean isRetry
    ) {
        if (connectTimeout > 0) {
            finalConnectTimeout = connectTimeout;
        }
        if (writeTimeout > 0) {
            finalWriteTimeout = writeTimeout;
        }
        if (readTimeout > 0) {
            finalReadTimeout = readTimeout;
        }
        OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder();
        builder.sslSocketFactory(sslSocketFactory(), trustAllCerts[0]);
        builder.writeTimeout(finalConnectTimeout, TimeUnit.SECONDS);
        builder.writeTimeout(finalWriteTimeout, TimeUnit.SECONDS);
        builder.readTimeout(finalReadTimeout, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(isRetry);
        return builder.build();
    }

    private static final X509TrustManager[] trustAllCerts = new X509TrustManager[1];

    static {
        trustAllCerts[0] = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private static SSLSocketFactory sslSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("fail to create sslSocketFactory");
    }

    private static Request.Builder getBuilder(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (null != headers) {
            builder.headers(Headers.of(headers));
        }
        return builder;
    }

    /**
     * http get方式请求，返回json格式响应报文
     *
     * @param url 请求路径
     * @return json格式响应报文
     */
    public static String doGet(String url) {
        return doGet(url, null);
    }

    /**
     * http get方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doGet(String url, long connectTimeout, long writeTimeout, long readTimeout) {
        return doGet(url, null, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http get方式请求，返回json格式响应报文
     *
     * @param url     请求路径
     * @param headers 请求头
     * @return json格式响应报文
     */
    public static String doGet(String url, Map<String, String> headers) {
        return doGet(url, headers, -1, -1, -1);
    }

    /**
     * http get方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param headers        请求头
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doGet(
        String url,
        Map<String, String> headers,
        long connectTimeout,
        long writeTimeout,
        long readTimeout
    ) {
        Request.Builder builder = getBuilder(url, headers);
        Request request = builder.get().build();
        return doHttp(request, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http post方式请求，返回json格式响应报文
     *
     * @param url       请求路径
     * @param jsonParam json格式参数
     * @return json格式响应报文
     */
    public static String doPost(String url, String jsonParam) {
        return doPost(url, jsonParam, null);
    }

    /**
     * http post方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param jsonParam      json格式参数
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doPost(
        String url,
        String jsonParam,
        long connectTimeout,
        long writeTimeout,
        long readTimeout
    ) {
        return doPost(url, jsonParam, null, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http post方式请求，返回json格式响应报文
     *
     * @param url       请求路径
     * @param jsonParam json格式参数
     * @param headers   请求头
     * @return json格式响应报文
     */
    public static String doPost(String url, String jsonParam, Map<String, String> headers) {
        return doPost(url, jsonParam, headers, -1, -1, -1);
    }

    /**
     * http post方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param jsonParam      json格式参数
     * @param headers        请求头
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doPost(
        String url,
        String jsonParam,
        Map<String, String> headers,
        long connectTimeout,
        long writeTimeout,
        long readTimeout
    ) {
        Request.Builder builder = getBuilder(url, headers);
        RequestBody body = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), jsonParam);
        Request request = builder.post(body).build();
        return doHttp(request, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http put方式请求，返回json格式响应报文
     *
     * @param url       请求路径
     * @param jsonParam json格式参数
     * @return json格式响应报文
     */
    public static String doPut(String url, String jsonParam) {
        return doPut(url, jsonParam, null);
    }

    /**
     * http put方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param jsonParam      json格式参数
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doPut(
        String url,
        String jsonParam,
        long connectTimeout,
        long writeTimeout,
        long readTimeout
    ) {
        return doPut(url, jsonParam, null, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http put方式请求，返回json格式响应报文
     *
     * @param url       请求路径
     * @param jsonParam json格式参数
     * @param headers   请求头
     * @return json格式响应报文
     */
    public static String doPut(String url, String jsonParam, Map<String, String> headers) {
        return doPut(url, jsonParam, headers, -1, -1, -1);
    }

    /**
     * http put方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param jsonParam      json格式参数
     * @param headers        请求头
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doPut(
        String url,
        String jsonParam,
        Map<String, String> headers,
        long connectTimeout,
        long writeTimeout,
        long readTimeout
    ) {
        Request.Builder builder = getBuilder(url, headers);
        RequestBody body = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), jsonParam);
        Request request = builder.put(body).build();
        return doHttp(request, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http delete方式请求，返回json格式响应报文
     *
     * @param url 请求路径
     * @return json格式响应报文
     */
    public static String doDelete(String url) {
        return doDelete(url, null, null);
    }

    /**
     * http delete方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doDelete(String url, long connectTimeout, long writeTimeout, long readTimeout) {
        return doDelete(url, null, null, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http delete方式请求，返回json格式响应报文
     *
     * @param url     请求路径
     * @param headers 请求头
     * @return json格式响应报文
     */
    public static String doDelete(String url, Map<String, String> headers) {
        return doDelete(url, headers, null, -1, -1, -1);
    }

    public static String doDelete(String url, String body, Map<String, String> headers) {
        return doDelete(url, headers, body, -1, -1, -1);
    }

    /**
     * http delete方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param headers        请求头
     * @param body           请求体
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doDelete(
        String url,
        Map<String, String> headers,
        String body,
        long connectTimeout,
        long writeTimeout,
        long readTimeout
    ) {
        Request.Builder builder = getBuilder(url, headers);
        Request request;
        if (StringUtils.isBlank(body)) {
            request = builder.delete().build();
        } else {
            request = builder.delete(RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), body)).build();
        }
        return doHttp(request, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http方式请求，返回响应报文
     *
     * @param request    okhttp请求体
     * @return json格式响应报文
     */
    public static String doHttp(Request request) {
        return doHttp(request, finalConnectTimeout, finalWriteTimeout, finalReadTimeout);
    }

    /**
     * http方式请求，返回响应报文
     *
     * @param request    okhttp请求体
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doHttp(Request request, long connectTimeout, long writeTimeout, long readTimeout) {
        OkHttpClient httpClient = createClient(connectTimeout, writeTimeout, readTimeout);
        Response response = null;
        String responseContent = null;
        try {
            response = httpClient.newCall(request).execute();
            assert response.body() != null;
            responseContent = response.body().string();
        } catch (IOException e) {
            logger.error("http send  throw Exception", e);
        } finally {
            if (response != null) {
                assert response.body() != null;
                response.body().close();
            }
        }
        if (response != null && !response.isSuccessful()) {
            logger.error("Fail to request(" + request + ") with code " + response.code()
                + " , message " + response.message() + " and response" + responseContent);
        }
        return responseContent;
    }

    /**
     * http方式请求，返回response响应对象
     *
     * @param request    okhttp请求体
     * @return response响应对象
     */
    public static Response doHttpRaw(Request request) {
        return doHttpRaw(request, finalConnectTimeout, finalWriteTimeout, finalReadTimeout, false);
    }

    /**
     * http方式请求，返回response响应对象
     *
     * @param request    okhttp请求体
     * @param isRetry    是否重试
     * @return response响应对象
     */
    public static Response doHttpRaw(Request request, boolean isRetry) {
        return doHttpRaw(request, finalConnectTimeout, finalWriteTimeout, finalReadTimeout, isRetry);
    }

    /**
     * http方式请求，返回response响应对象
     *
     * @param request    okhttp请求体
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @param isRetry    是否重试
     * @return response响应对象
     */
    public static Response doHttpRaw(
        Request request,
        long connectTimeout,
        long writeTimeout,
        long readTimeout,
        boolean isRetry
    ) {
        OkHttpClient httpClient = createRetryOptionClient(connectTimeout, writeTimeout, readTimeout, isRetry);
        try {
            return httpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 下载文件到目标路径
     */
    public static void downloadFile(String url, File destPath) {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        OkHttpClient httpClient = createLongClient();
        try (Response response = httpClient.newCall(request).execute()) {
            int code = response.code();
            if (code == 404) {
                logger.warn("The file {} is not exist", url);
                throw new RuntimeException("文件不存在");
            }
            if (!response.isSuccessful()) {
                String message = response.message();
                logger.warn("fail to download the file from {} because of {} and code {}", url, message, code);
                throw new RuntimeException("获取文件失败");
            }
            if (!destPath.getParentFile().exists()) {
                destPath.getParentFile().mkdirs();
            }
            byte[] buf = new byte[4096];
            try (InputStream bs = Objects.requireNonNull(response.body()).byteStream()) {
                int len = bs.read(buf);
                try (FileOutputStream fos = new FileOutputStream(destPath)) {
                    while (len != -1) {
                        fos.write(buf, 0, len);
                        len = bs.read(buf);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载文件到目标路径
     *
     * @param response  响应对象
     * @param destPath 目标路径
     */
    public static void downloadFile(Response response, File destPath) {
        int code = response.code();
        if (response.code() == 304) {
            logger.info("file is newest, do not download to {}", destPath);
            return;
        }
        if (!response.isSuccessful()) {
            String message = response.message();
            logger.warn("fail to download the file because of {} and code {}", message, code);
            throw new RuntimeException("获取文件失败");
        }
        if (!destPath.getParentFile().exists()) {
            destPath.getParentFile().mkdirs();
        }
        byte[] buf = new byte[4096];

        try (InputStream bs = Objects.requireNonNull(response.body()).byteStream()) {
            int len = bs.read(buf);
            try (FileOutputStream fos = new FileOutputStream(destPath)) {
                while (len != -1) {
                    fos.write(buf, 0, len);
                    len = bs.read(buf);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
