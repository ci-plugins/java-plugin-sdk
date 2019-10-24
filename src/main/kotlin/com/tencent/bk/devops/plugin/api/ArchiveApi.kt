package com.tencent.bk.devops.plugin.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.ceil


class ArchiveApi {

    private val okHttpClient: OkHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(5L, TimeUnit.SECONDS)
            .readTimeout(300 * 5L, TimeUnit.SECONDS) // Set to 15 minutes
            .writeTimeout(60L, TimeUnit.SECONDS)
            .build()

    public fun download(uri: String, request: Request, destPath: File, size: Long, bkWorkSpace: String) {
        okHttpClient.newBuilder().build().newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("get $request failured......,please check your input")
            download(uri, response, destPath, size, bkWorkSpace)
        }
    }

    private fun download(uri: String, response: Response, destPath: File, size: Long, bkWorkSpace: String) {
        if (response.code() == 404) {
            throw RuntimeException("文件不存在")
        }
        if (!response.isSuccessful) {
//            LoggerService.addNormalLine(response.body()!!.string())
            throw RuntimeException("获取文件失败")
        }
        if (!destPath.parentFile.exists()) destPath.parentFile.mkdirs()
//        LoggerService.addNormalLine("save file >>>> ${destPath.canonicalPath}")
        logger.info("save file >>>> ${destPath.canonicalPath}")
        var process = 0L
        response.body()!!.byteStream().use { bs ->
            val buf = ByteArray(4096)
            var len = bs.read(buf)
            process = writeStateToTxt(process, size, bkWorkSpace, uri, len,true)
            FileOutputStream(destPath).use { fos ->
                while (len != -1) {
                    fos.write(buf, 0, len)
                    len = bs.read(buf)
                    process = writeStateToTxt(process, size, bkWorkSpace, uri, len,false)
                }
            }
        }
    }

    private fun writeStateToTxt(process: Long, size: Long, bkWorkSpace: String, uri: String, len: Int, isFirst: Boolean): Long {
        val l = process + len
        val countResult = if (size != 0L) ceil(((process / size) * 100).toDouble()) else 0
        val log = "$uri :$countResult%\n"
        logger.info(log)
        var txtFile = File("$bkWorkSpace${File.separator}BKCIArchiveDownLoadState.txt")
        if (isFirst) {
            if (txtFile.exists()) {
                txtFile.delete()
            }
            txtFile.createNewFile()
        }
        FileUtils.write(txtFile, log, "UTF-8", true)
        return l
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ArchiveApi::class.java)
    }
}