package com.tencent.bk.devops.plugin.api

import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.exception.AtomException
import com.tencent.bk.devops.atom.exception.RemoteServiceException
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.net.HttpRetryException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

abstract class AbstractBaseApi : BaseApi() {

    companion object {
        private val retryCodes = arrayOf(502, 503, 504)
        private const val DEFAULT_RETRY_TIME = 5
        val logger = LoggerFactory.getLogger(AbstractBaseApi::class.java)!!
        private const val sleepTimeMills = 500L
    }

    protected fun requestForResponse(
        request: Request,
        connectTimeoutInSec: Long? = null,
        readTimeoutInSec: Long? = null,
        writeTimeoutInSec: Long? = null,
        retryCount: Int = DEFAULT_RETRY_TIME
    ): Response {
        val builder = okHttpClient.newBuilder()
        if (connectTimeoutInSec != null) {
            builder.connectTimeout(connectTimeoutInSec, TimeUnit.SECONDS)
        }
        if (readTimeoutInSec != null) {
            builder.readTimeout(readTimeoutInSec, TimeUnit.SECONDS)
        }
        if (writeTimeoutInSec != null) {
            builder.writeTimeout(writeTimeoutInSec, TimeUnit.SECONDS)
        }
        val httpClient = builder.build()
        val retryFlag = try {
            val response = httpClient.newCall(request).execute()

            if (retryCodes.contains(response.code())) { // 网关502,503，可重试
                true
            } else {
                return response
            }
        } catch (e: UnknownHostException) { // DNS问题导致请求未到达目标，可重试
            logger.warn("UnknownHostException|request($request),error is :$e, try to retry $retryCount")
            retryCount > 0
        } catch (e: ConnectException) {
            logger.warn("ConnectException|request($request),error is :$e, try to retry $retryCount")
            retryCount > 0
        } catch (re: SocketTimeoutException) {
            if (re.message == "connect timed out" ||
                (request.method() == "GET" && re.message == "timeout")
            ) {
                logger.warn("SocketTimeoutException(${re.message})|request($request), try to retry $retryCount")
                retryCount > 0
            } else { // 对于因为服务器的超时，不一定能幂等重试的，抛出原来的异常，外层业务自行决定是否重试
                logger.error("Fail to request($request),error is :$re", re)
                throw re
            }
        } catch (error: Exception) {
            logger.error("Fail to request($request),error is :$error", error)
            throw RemoteServiceException("Fail to request($request),error is:${error.message}", 500, "")
        }

        if (retryFlag && retryCount > 0) {
            logger.warn(
                "Fail to request($request), retry after $sleepTimeMills ms"
            )
            Thread.sleep(sleepTimeMills)
            return requestForResponse(
                request, connectTimeoutInSec, readTimeoutInSec, writeTimeoutInSec, retryCount - 1
            )
        } else {
            logger.error("Fail to request($request), try to retry $DEFAULT_RETRY_TIME")
            throw HttpRetryException("Fail to request($request), try to retry $DEFAULT_RETRY_TIME", 999)
        }
    }

    protected fun retryRequest(
        request: Request,
        errorMessage: String,
        connectTimeoutInSec: Long? = null,
        readTimeoutInSec: Long? = null,
        writeTimeoutInSec: Long? = null
    ): String {

        requestForResponse(
            request = request,
            connectTimeoutInSec = connectTimeoutInSec,
            readTimeoutInSec = readTimeoutInSec,
            writeTimeoutInSec = writeTimeoutInSec
        ).use { response ->
            if (!response.isSuccessful) {
                val responseContent = response.body()?.string()
                logger.warn(
                    "Fail to request($request) with code ${response.code()} ," +
                        " message ${response.message()} and response ($responseContent)"
                )
                throw RemoteServiceException(errorMessage, response.code(), responseContent)
            }
            return response.body()!!.string()
        }
    }
}
