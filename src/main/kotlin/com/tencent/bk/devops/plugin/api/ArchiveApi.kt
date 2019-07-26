package com.tencent.bk.devops.plugin.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


class ArchiveApi {
    private val okHttpClient: OkHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(5L, TimeUnit.SECONDS)
            .readTimeout(300*5L, TimeUnit.SECONDS) // Set to 15 minutes
            .writeTimeout(60L, TimeUnit.SECONDS)
            .build()
    public fun download(request: Request, destPath: File) {
        okHttpClient.newBuilder().build().newCall(request).execute().use { response ->
            download(response, destPath)
        }
    }

    private fun download(response: Response, destPath: File) {
        if (response.code() == 404) {
            throw RuntimeException("文件不存在")
        }
        if (!response.isSuccessful) {
//            LoggerService.addNormalLine(response.body()!!.string())
            throw RuntimeException("获取文件失败")
        }
        if (!destPath.parentFile.exists()) destPath.parentFile.mkdirs()
//        LoggerService.addNormalLine("save file >>>> ${destPath.canonicalPath}")

        response.body()!!.byteStream().use { bs ->
            val buf = ByteArray(4096)
            var len = bs.read(buf)
            FileOutputStream(destPath).use { fos ->
                while (len != -1) {
                    fos.write(buf, 0, len)
                    len = bs.read(buf)
                }
            }
        }
    }
}