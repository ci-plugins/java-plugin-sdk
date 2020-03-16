package com.tencent.bk.devops.plugin.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class ArchiveApi {

    private val okHttpClient: OkHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(5L, TimeUnit.SECONDS)
        .readTimeout(300 * 5L, TimeUnit.SECONDS) // Set to 15 minutes
        .writeTimeout(60L, TimeUnit.SECONDS)
        .build()

    public fun download(uri: String, request: Request, destPath: File, size: Long, bkWorkSpace: String) {
        okHttpClient.newBuilder().build().newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("get $request failed, response code: ${response.code()}")
            download(response, destPath, size)
        }
    }

    private fun download(response: Response, destPath: File, size: Long) {
        if (response.code() == 404) {
            throw RuntimeException("文件不存在")
        }
        if (!response.isSuccessful) {
            throw RuntimeException("获取文件失败")
        }
        if (!destPath.parentFile.exists()) destPath.parentFile.mkdirs()
        val target = destPath.canonicalPath
        logger.info("save file to: $target, size $size byte(s)")
        var readBytes = 0L
        var doubleSize = size.toDouble()
        var startTime = System.currentTimeMillis()
        response.body()!!.byteStream().use { bs ->
            val buf = ByteArray(4096)
            var logTime = System.currentTimeMillis()
            var len = bs.read(buf)
            FileOutputStream(destPath).use { fos ->
                logger.info("$target >>> 0.0%\n")
                while (len != -1) {
                    fos.write(buf, 0, len)
                    len = bs.read(buf)
                    readBytes += len
                    val now = System.currentTimeMillis()
                    if ((now - logTime) > 3000) {
                        logger.info("$target >>> ${String.format("%.1f", readBytes / doubleSize * 100)}%\n")
                        logTime = now
                    }
                }
                logger.info("$target >>> 100%\n")
            }
        }
        logger.info("file transfer time: ${String.format("%.2f", (System.currentTimeMillis() - startTime) / 1000.0)} seconds")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ArchiveApi::class.java)
    }
}