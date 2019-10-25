package com.tencent.bk.devops.plugin.api.cos

import com.google.common.io.Files
import com.google.gson.JsonParser
import com.tencent.bk.devops.plugin.api.impl.ArtifactoryApi
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.utils.OkhttpUtils
import com.tencent.devops.common.cos.COSClientConfig
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class UploadCosCdn {
    private var count = 0
    private val parser = JsonParser()
    private val trunkSize = 4 * 1024 * 1024
    private var currentPos: Long = 0
    private val refreshToken = "2c224976-04ef-11e9-ba56-60def3767b57"
    private val refreshPlatIds = "200042,200002,170,166,161,200004,200005,200001,200437,90"
    private val refreshUrl = "http://refresh.api.hycdn.oa.com:27591/refresh?type=url&plat_ids=$refreshPlatIds&format=plain"
    private val artifactoryApi = ArtifactoryApi()
    var cosService: CosService? = null
    var uploadCosCdnParam: UploadCosCdnParam? = null
    var projectId: String = ""
    var pipelineId: String = ""
    var buildId: String = ""
    var elementId: String = ""
    var executeCount: Int = 1

    constructor()

    constructor(
        cosService: CosService,
        uploadCosCdnParam: UploadCosCdnParam
    ) : this() {
        this.cosService = cosService
        this.uploadCosCdnParam = uploadCosCdnParam
        this.projectId = uploadCosCdnParam.projectId
        this.pipelineId = uploadCosCdnParam.pipelineId
        this.buildId = uploadCosCdnParam.buildId
        this.elementId = uploadCosCdnParam.elementId
    }

    public fun upload(): MutableList<Map<String, String>> {
        try {
            return uploadFileToCos(uploadCosCdnParam!!.regexPaths, uploadCosCdnParam!!.customize, uploadCosCdnParam!!.bucket,
                    uploadCosCdnParam!!.cdnPath, uploadCosCdnParam!!.domain, uploadCosCdnParam!!.cosClientConfig)
        } catch (ex: Exception) {
            logger.error("Execute Upload to cos cdn exception: ${ex.message}", ex)
        }
        return mutableListOf()
    }

    private fun uploadFileToCos(
        regexPaths: String,
        customize: Boolean,
        bucket: String,
        cdnPath: String,
        domain: String,
        cosClientConfig: COSClientConfig
    ): MutableList<Map<String, String>> {
//        val searchUrl = SdkEnv.genUrl("/jfrog/api/build/search/aql")
//        logger.info("the search url is :$searchUrl")
        val downloadUrlList = mutableListOf<Map<String, String>>()
        // 下载文件到临时目录，然后上传到COS
        var workspace = Files.createTempDir()
        try {
            var result: Result<List<String>>
            if (customize == false) {
                result = artifactoryApi.getArtifactoryFileUrl("PIPELINE", regexPaths)
            } else {
                result = artifactoryApi.getArtifactoryFileUrl("CUSTOM_DIR", regexPaths)
            }
            var count = 0
            result.data?.forEach { url ->
                if (count>10) {
                    logger.info("-----------------------------------")
                    workspace.deleteRecursively()
                    workspace = Files.createTempDir()
                    count = 0
                }
                val filename = url.substring(url.lastIndexOf("/"), url.indexOf("?"))
                val file = File(workspace, filename)
                val cdnFileName = cdnPath + file.name
                OkhttpUtils.downloadFile(url, file)
                val fileInputStream = FileInputStream(file)
                val md5 = DigestUtils.md5Hex(fileInputStream)
                val downloadUrl = uploadToCosImpl(cdnFileName, domain, file, cosClientConfig, bucket)
                downloadUrlList.add(mapOf("fileName" to file.name, "fileDownloadUrl" to downloadUrl, "md5" to md5))
                count += 1
            }
        } catch (ex: IOException) {
            val msg = String.format("Upload file failed because of IOException(%s)", ex.message)
            logger.error(msg, ex)
        } catch (ex: Exception) {
            val msg = String.format("Upload file failed because of Exception(%s)", ex.message)
            logger.error(msg, ex)
        } finally {
            workspace.deleteRecursively()
        }
        return downloadUrlList
    }

    private fun uploadToCosImpl(cdnFileName: String, domain: String, file: File, cosClientConfig: COSClientConfig, bucket: String): String {
        // 先删除cos上的文件，否则重复上传会导致失败
        cosService!!.deleteFile(cosClientConfig, bucket, cdnFileName)
        // 上传COS
        logger.info("Begin to upload to cos, fileName: $cdnFileName")
        val trunkSize = trunkSize
        val tmpContent = ByteArray(trunkSize)
        var readSize: Int

        FileInputStream(file).use { fis ->
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
        }
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

    companion object {
        private val logger = LoggerFactory.getLogger(UploadCosCdn::class.java)
    }
}