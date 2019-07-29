package com.tencent.bk.devops.plugin.api.cos

import com.google.common.io.Files
import com.google.gson.JsonParser
import com.tencent.bk.devops.atom.api.SdkEnv
import com.tencent.bk.devops.plugin.utils.JsonUtil
import com.tencent.bk.devops.plugin.utils.OkhttpUtils
import com.tencent.devops.common.cos.COSClientConfig
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Arrays

class UploadCosCdnThread : Runnable {
    private var count = 0
    private val parser = JsonParser()
    private val trunkSize = 4 * 1024 * 1024
    private var currentPos: Long = 0
    private val refreshToken = "2c224976-04ef-11e9-ba56-60def3767b57"
    private val refreshPlatIds = "200042,200002,170,166,161,200004,200005,200001,200437,90"
    private val refreshUrl = "http://refresh.api.hycdn.oa.com:27591/refresh?type=url&plat_ids=$refreshPlatIds&format=plain"

    var cosService: CosService? = null
    var mapOperation: MutableMap<String, String>? = null
    var uploadCosCdnParam: UploadCosCdnParam? = null
    var projectId: String = ""
    var pipelineId: String = ""
    var buildId: String = ""
    var elementId: String = ""
    var executeCount: Int = 1

    constructor()

    constructor(
            cosService: CosService,
            mapOperation: MutableMap<String, String>,
            uploadCosCdnParam: UploadCosCdnParam
    ) : this() {
        this.cosService = cosService
        this.mapOperation = mapOperation
        this.uploadCosCdnParam = uploadCosCdnParam
        this.projectId = uploadCosCdnParam.projectId
        this.pipelineId = uploadCosCdnParam.pipelineId
        this.buildId = uploadCosCdnParam.buildId
        this.elementId = uploadCosCdnParam.elementId
    }

    override fun run() {
        try {
            uploadFileToCos(uploadCosCdnParam!!.regexPaths, uploadCosCdnParam!!.customize, uploadCosCdnParam!!.bucket,
                    uploadCosCdnParam!!.cdnPath, uploadCosCdnParam!!.domain, uploadCosCdnParam!!.cosClientConfig)
        } catch (ex: Exception) {
            logger.error("Execute Upload to cos cdn exception: ${ex.message}", ex)
        }
    }

    private fun uploadFileToCos(
            regexPaths: String,
            customize: Boolean,
            bucket: String,
            cdnPath: String,
            domain: String,
            cosClientConfig: COSClientConfig
    ): MutableList<Map<String, String>> {
        val searchUrl = SdkEnv.genUrl("/jfrog/api/service/search/aql")
        val downloadUrlList = mutableListOf<Map<String, String>>()

        // 下载文件到临时目录，然后上传到COS
        val workspace = Files.createTempDir()
        try {
            count = 0
            regexPaths.split(",").forEach { regex ->
                val requestBody = getRequestBody(regex, customize)
                logger.info("Get file request body: $requestBody")
                val request = Request.Builder()
                        .url(searchUrl)
                        .post(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), requestBody))
                        .build()

                OkhttpUtils.doHttp(request).use { response ->
                    val body = response.body()!!.string()

                    val results = parser.parse(body).asJsonObject["results"].asJsonArray

                    logger.info("There are ${results.size()} file(s) match $regex")
                    for (i in 0 until results.size()) {
                        count++
                        val obj = results[i].asJsonObject
                        val path = getPath(obj["path"].asString, obj["name"].asString, customize)
                        val url = getUrl(path, customize)
                        // val filePath = getFilePath(obj["path"].asString, obj["name"].asString, isCustom)
                        val file = File(workspace, obj["name"].asString)
                        OkhttpUtils.downloadFile(url, file)

                        val cdnFileName = cdnPath + file.name
                        val downloadUrl = uploadToCosImpl(cdnFileName, domain, file, cosClientConfig, bucket)
                        downloadUrlList.add(mapOf("fileName" to file.name, "fileDownloadUrl" to downloadUrl))
                        setProcessToMap(1, count, null)
                    }
                }
            }
            if (count == 0) {
                setProcessToMap(2, 0, null)
                logger.info("No file distributed")
            } else {
                setProcessToMap(0, count, downloadUrlList)
                logger.info("$count file(s) have been distributed")
            }

//            if (count == 0) throw RuntimeException("No file distributed")
        } catch (ex: IOException) {
            setProcessToMap(2, 0, null)
            val msg = String.format("Upload file failed because of IOException(%s)", ex.message)
            logger.error(msg, ex)
        } /*catch (ex: COSException) {
            setProcessToMap(2, 0, null)
            val msg = String.format("Upload file failed because of COSException(%s)", ex.message)
            logger.error(msg, ex)
        }*/ catch (ex: Exception) {
            setProcessToMap(2, 0, null)
            val msg = String.format("Upload file failed because of Exception(%s)", ex.message)
            logger.error(msg, ex)
        } finally {
            workspace.deleteRecursively()
        }
        return downloadUrlList
    }

    /*
     * status: 状态，0：成功，1：正在执行，2：失败
     */
    private fun setProcessToMap(status: Int, count: Int, resultListMap: MutableList<Map<String, String>>?) {
        val key = "upload_cdn_task_${projectId}_${pipelineId}_${buildId}_$elementId"
        mapOperation!!.put(key + "_status", status.toString())
        mapOperation!!.put(key + "_count", count.toString())
        if (null != resultListMap) {
            val resultJson = JsonUtil.toJson(resultListMap)
            mapOperation!!.put(key + "_result", resultJson)
        }
    }

    private fun uploadToCosImpl(cdnFileName: String, domain: String, file: File, cosClientConfig: COSClientConfig, bucket: String): String {
        // 先删除cos上的文件，否则重复上传会导致失败
        cosService!!.deleteFile(cosClientConfig, bucket, cdnFileName)
        // 上传COS
        logger.info("Begin to upload to cos, fileName: $cdnFileName")
        val trunkSize = trunkSize
        val tmpContent = ByteArray(trunkSize)
        var readSize: Int

        FileInputStream(file).use({ fis ->
            readSize = fis.read(tmpContent)
            currentPos = 0
            while (readSize != -1) {
                val content = if (readSize == trunkSize) tmpContent else Arrays.copyOf(tmpContent, readSize)
                val nextPos = cosService!!.append(
                        cosClientConfig,
                        bucket,
                        cdnFileName,
                        null,
                        content,
                        currentPos,
                        "application/octet-stream"
                )
                currentPos = nextPos
                readSize = fis.read(tmpContent)
            }
        })
        logger.info("Upload to cos success, fileName: $cdnFileName")
        val downloadUrl = domain + cdnFileName // download url from cdn
        logger.info("Download url: $downloadUrl")
        // 刷新spm，防止出现同名文件上传时，边缘cdn节点的文件不能更新的问题
        refreshSpm(downloadUrl)
        return downloadUrl
    }

    private fun refreshSpm(downloadUrl: String): Boolean {
        val requestUrl = refreshUrl
        val requestBody = if (!downloadUrl.startsWith("http://")) "http://$downloadUrl" else downloadUrl
        logger.info("refresh spm requestUrl: $requestUrl")
        logger.info("refresh spm requestBody: $requestBody")
        val request = Request.Builder()
                .url(requestUrl)
                .addHeader("X-REFRESH-TOKEN", refreshToken)
                .post(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), requestBody))
                .build()

        OkhttpUtils.doHttp(request).use { response ->
            val body = response.body()!!.string()
            logger.info("refresh spm responseBody: $body")
            val responseBody = parser.parse(body).asJsonObject
            return if (responseBody["result"].asInt == 0) {
                logger.info("refresh spm success")
                true
            } else {
                logger.error("refresh spm failed!")
                false
            }
        }
    }

    private fun getRequestBody(regex: String, isCustom: Boolean): String {
        val pathPair = getPathPair(regex)
        return if (isCustom) {
            "items.find(\n" +
                    "    {\n" +
                    "        \"repo\":{\"\$eq\":\"generic-local\"}, \"path\":{\"\$eq\":\"bk-custom/$projectId${pathPair.first}\"}, \"name\":{\"\$match\":\"${pathPair.second}\"}\n" +
                    "    }\n" +
                    ")"
        } else {
            "items.find(\n" +
                    "    {\n" +
                    "        \"repo\":{\"\$eq\":\"generic-local\"}, \"path\":{\"\$eq\":\"bk-archive/$projectId/$pipelineId/$buildId${pathPair.first}\"}, \"name\":{\"\$match\":\"${pathPair.second}\"}\n" +
                    "    }\n" +
                    ")"
        }
    }

    // aa/test/*.txt
    // first = /aa/test
    // second = *.txt
    private fun getPathPair(regex: String): Pair<String, String> {
        if (regex.endsWith("/")) return Pair("/" + regex.removeSuffix("/"), "*")
        val index = regex.lastIndexOf("/")

        if (index == -1) return Pair("", regex) // a.txt

        return Pair("/" + regex.substring(0, index), regex.substring(index + 1))
    }

    // 处理jfrog传回的路径
    private fun getPath(path: String, name: String, isCustom: Boolean): String {
        return if (isCustom) {
            path.substring(path.indexOf("/") + 1).removePrefix("/$projectId") + "/" + name
        } else {
            path.substring(path.indexOf("/") + 1).removePrefix("/$projectId/$pipelineId/$buildId") + "/" + name
        }
    }

    // 生成保存本地的路径
    private fun getFilePath(path: String, name: String, isCustom: Boolean): String {
        return if (isCustom) {
            path.substring(path.indexOf("/")).removePrefix("/$projectId") + "/" + name
        } else {
            path.substring(path.indexOf("/")).removePrefix("/$projectId/$pipelineId/$buildId") + "/" + name
        }
    }

    // 获取jfrog传回的url
    private fun getUrl(realPath: String, isCustom: Boolean): String {
        return if (isCustom) {
            SdkEnv.genUrl("/jfrog/storage/service/custom/$realPath")
        } else {
            SdkEnv.genUrl("/jfrog/storage/service/archive/$realPath")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UploadCosCdnThread::class.java)
    }
}
