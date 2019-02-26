package com.tencent.bk.devops.atom.utils.http;

import com.tencent.bk.devops.atom.exception.AtomException;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class HttpClientUtil {

	private static final int MAP_TYPE = 1;

	private static final int JSON_TYPE = 2;

	private static final String APPLICATION_JSON = "application/json";

	private final static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);


    /**
     * http post方式请求，返回map格式响应报文
     * @param url 请求路径
     * @param jsonString json格式参数
     * @param timeout 超时时间（单位：秒）
     * @return map格式响应报文
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> postJsonParamAsMap(String url, String jsonString, int timeout) {
        HttpPost post = new HttpPost(url);
        if (StringUtils.isNotEmpty(jsonString)) {
            buildJsonParam(jsonString, post);
        }
        Object object = newHttpClient(post, MAP_TYPE, timeout);
        return object != null ? (Map<String, Object>) object : null;
    }


    /**
     * http post方式请求，返回map格式响应报文
     * @param url 请求路径
     * @param jsonString json格式参数
     * @param timeout 超时时间（单位：秒）
     * @return json格式响应报文
     */
	public static String postJsonParamAsJson(String url, String jsonString, int timeout){
		HttpPost post = new HttpPost(url);
		if (StringUtils.isNotEmpty(jsonString)) {
			buildJsonParam(jsonString, post);
		}
		return (String) newHttpClient(post, HttpClientUtil.JSON_TYPE, timeout);
	}

    /**
     * http post方式请求，返回map格式响应报文
     * @param url 请求路径
     * @param paramMap map格式参数
     * @param timeout 超时时间（单位：秒）
     * @return map格式响应报文
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> postMapParamAsMap(String url, Map<String, Object> paramMap, int timeout) {
        HttpPost post = new HttpPost(url);
        if (paramMap != null && paramMap.size() != 0) {
            buildJsonParam(paramMap, post);
        }
        Object object = newHttpClient(post, HttpClientUtil.MAP_TYPE, timeout);
        return object != null ? (Map<String, Object>) object : null;
    }


    /**
     * http post方式请求，返回json格式响应报文
     * @param url 请求路径
     * @param paramMap map格式参数
     * @param timeout 超时时间（单位：秒）
     * @return json格式响应报文
     */
    public static String postMapParamAsJson(String url, Map<String, Object> paramMap, int timeout) {
        HttpPost post = new HttpPost(url);
        if (paramMap != null && paramMap.size() != 0) {
            buildJsonParam(paramMap, post);
        }
        return (String) newHttpClient(post, HttpClientUtil.JSON_TYPE, timeout);
    }

    /**
     * http get方式请求，返回json格式响应报文
     * @param url 请求路径
     * @return json格式响应报文
     */
    public static String getAsJson(String url) {
        HttpGet get = new HttpGet(url);
        Object object = newHttpClient(get, HttpClientUtil.JSON_TYPE, 10);
        return object != null ? (String) object : null;
    }


    /**
     * http get方式请求，返回map格式响应报文
     * @param url 请求路径
     * @return map格式响应报文
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getAsMap(String url) {
        HttpGet get = new HttpGet(url);
        Object object = newHttpClient(get, HttpClientUtil.MAP_TYPE, 10);
        return object != null ? (Map<String, Object>) object : null;
    }


    /**
     * http put方式请求，返回json格式响应报文
     * @param url 请求路径
     * @param paramMap map格式参数
     * @return json格式响应报文
     */
    public static String putMapParamAsString(String url, Map<String, Object> paramMap) {
        HttpPut put = new HttpPut(url);
        if (paramMap != null && paramMap.size() != 0) {
            buildJsonParam(paramMap, put);
        }
        Object object = newHttpClient(put, JSON_TYPE, 10);
        return object != null ? (String) object : null;
    }


    /**
     * http put方式请求，返回json格式响应报文
     * @param url 请求路径
     * @param jsonString json格式参数
     * @param timeout 超时时间（单位：秒）
     * @return json格式响应报文
     */
    public static String putJsonParamAsString(String url, String jsonString, int timeout) {
        HttpPut put = new HttpPut(url);
        if (StringUtils.isNotEmpty(jsonString)) {
            buildJsonParam(jsonString, put);
        }

        Object object = newHttpClient(put, JSON_TYPE, timeout);
        return object != null ? (String) object : null;
    }


    /**
     * http delete方式请求，返回json格式响应报文
     * @param url 请求路径
     * @param timeout 超时时间（单位：秒）
     * @return json格式响应报文
     */
    public static String deleteAsString(String url, int timeout) {
        HttpDelete delete = new HttpDelete(url);
        Object object = newHttpClient(delete, JSON_TYPE, timeout);
        return object != null ? (String) object : null;
    }


	private static Object newHttpClient(HttpRequestBase http, int type, int timeout) {
        HttpClient httpClient = HttpClientBuilder.create().build();
		try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000)
                    .setSocketTimeout(timeout * 1000).build();
			http.setConfig(requestConfig);
			HttpResponse response = httpClient.execute(http);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				String res = getResponseBodyString(response.getEntity().getContent());
				if (res.length() != 0) {
					if (type == MAP_TYPE) {
						return JsonUtil.fromJson(res, Map.class);
					} else {
						return res;
					}
				}
			} else {
				logger.error("newHttpClient has response error!reposne code is " + statusCode);
				throw new AtomException("newHttpClient has response error!reposne code is " + statusCode);
			}
		} catch (ConnectionPoolTimeoutException e) {
			logger.error("newHttpClient connection pool time out, " + e.getMessage(), e);
			throw new AtomException( "newHttpClient connection time out");
        } catch (ConnectTimeoutException e) {
            logger.error("newHttpClient connection time out, " + e.getMessage(), e);
            throw new AtomException("newHttpClient connection time out");
        } catch (SocketTimeoutException e) {
            logger.error("newHttpClient socket time out, " + e.getMessage(), e);
            throw new AtomException("SocketTimeoutException");
        } catch (ClientProtocolException e) {
            logger.error("newHttpClient has client protocol exception. " + e.getMessage(), e);
            throw new AtomException("newHttpClient has client protocol exception");
        } catch (IllegalStateException e) {
            logger.error("newHttpClient has illegal state exception. " + e.getMessage(), e);
            throw new AtomException("newHttpClient IllegalStateException");
        } catch (IOException e) {
            logger.error("newHttpClient has io exception. " + e.getMessage(), e);
            throw new AtomException("newHttpClient IOException");
        } catch (Exception e) {
            logger.error("newHttpClient has exception. " + e.getMessage(), e);
            throw new AtomException("exception");
        } finally {
            // 关闭请求
            http.releaseConnection();
        }
		return null;
	}

	private static String getResponseBodyString(InputStream inputStream) {
	    return getResponseBodyString(inputStream,StandardCharsets.UTF_8.name());
	}

	private static String getResponseBodyString(InputStream inputStream, String charset) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			if (inputStream == null) {
				return null;
			}
			reader = new BufferedReader(new InputStreamReader(inputStream, charset));
			String str;
			while ((str = reader.readLine()) != null) {
				sb.append(str);
			}
			return sb.toString();
		} catch (IOException e) {
			logger.error("read http response body stream has error. " + e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("close http response body stream has error. " + e.getMessage(), e);
				}
			}
		}
		return null;
	}

	private static void buildJsonParam(Map<String, Object> paramMap, HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase) {
		String jsonString = JsonUtil.toJson(paramMap);
        buildJsonParam(jsonString,httpEntityEnclosingRequestBase);
	}

	private static void buildJsonParam(String jsonString, HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase) {
		StringEntity params = new StringEntity(jsonString, StandardCharsets.UTF_8);
		httpEntityEnclosingRequestBase.addHeader("content-type", APPLICATION_JSON);
		httpEntityEnclosingRequestBase.setEntity(params);
	}


}
