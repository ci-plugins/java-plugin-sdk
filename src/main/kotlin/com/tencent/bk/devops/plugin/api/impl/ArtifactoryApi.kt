package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.api.SdkEnv
import com.tencent.bk.devops.atom.pojo.artifactory.FileDetail
import com.tencent.bk.devops.atom.utils.http.SdkUtils
import com.tencent.bk.devops.atom.utils.json.JsonUtil
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.pojo.artifactory.JfrogFilesData
import com.tencent.bk.devops.plugin.utils.OkhttpUtils
import okhttp3.Request
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList
import java.util.regex.Pattern

class ArtifactoryApi : BaseApi() {

    /**
     * 获取构建文件下载路径
     * @param artifactoryType 版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path 路径 多路径使用","或者";"分割
     * @return 文件下载路径数组
     */
    fun getArtifactoryFileUrl(artifactoryType: String, path: String): Result<List<String>> {
        val urlBuilder = StringBuilder("/artifactory/api/build/artifactories/thirdPartyDownloadUrl?artifactoryType=")
        urlBuilder.append(artifactoryType).append("&path=").append(path).append("&ttl=").append(3600) // 下载链接有效期设定为1小时
        val requestUrl = urlBuilder.toString()
        logger.info("the requestUrl is:{}", requestUrl)
        val request = super.buildGet(urlBuilder.toString())
        var responseContent: String? = null
        try {
            responseContent = super.request(request, "获取包路径失败")
        } catch (e: IOException) {
            logger.error("get artifactoryFileUrl throw Exception", e)
        }

        return if (null != responseContent) {
            val srcUrlResult = JsonUtil.fromJson(responseContent, object : TypeReference<Result<List<String>>>() {
            })
            val srcUrlList = srcUrlResult.data
            val finalSrcUrlList = ArrayList<String>()
            if (srcUrlList != null) {
                for (srcUrl in srcUrlList) {
                    val finalSrcUrl = srcUrl.replace(getUrlHost(srcUrl), SdkEnv.getGatewayHost())
                    finalSrcUrlList.add(finalSrcUrl)
                }
            }
            logger.info("getArtifactoryFileUrl responseContent is:{}", JsonUtil.toJson(finalSrcUrlList))
             Result(finalSrcUrlList)
        } else {
            logger.info("getArtifactoryFileUrl responseContent is null")
            Result(emptyList())
        }
    }

    /**
     * 获取仓库的构件元数据
     * @param artifactoryType 版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path 文件路径 多路径使用","或者";"分割
     * @return 文件下载路径数组
     */
    fun getArtifactsProperties(artifactoryType: String, path: String): Result<List<FileDetail>?> {
        val urlBuilder = StringBuilder("/artifactory/api/build/artifactories/getPropertiesByRegex?artifactoryType=")
        urlBuilder.append(artifactoryType).append("&path=").append(path)
        val requestUrl = urlBuilder.toString()
        logger.info("the requestUrl is:{}", requestUrl)
        val request = super.buildGet(urlBuilder.toString())
        var responseContent: String? = null
        try {
            responseContent = super.request(request, "获取包路径失败")
        } catch (e: IOException) {
            logger.error("get artifactoryProperties throw Exception", e)
        }

        return if (null != responseContent) {
            val srcUrlResult = JsonUtil.fromJson(responseContent, object : TypeReference<Result<List<FileDetail>>>() {
            })
            val fileDetailList = srcUrlResult.data
            logger.info("getArtifactoryProperties responseContent is:{}", JsonUtil.toJson(fileDetailList))
            Result(fileDetailList)
        } else {
            logger.info("getArtifactoryProperties responseContent is null")
            Result(data = null)
        }
    }

    /**
     * 下载构建文件到本地
     * @param artifactoryType 版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path 路径
     * @return 文件在本地的保存路径
     */
    fun downloadArtifactoryFileToLocal(artifactoryType: String, path: String): Result<List<String>> {
        return downloadArtifactoryFileToLocal(artifactoryType, path, SdkUtils.getDataDir())
    }

    /**
     * 下载构建文件到本地
     * @param artifactoryType 版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path 路径
     * @param saveDir 文件保存目录
     * @return 文件在本地的保存路径
     */
    fun downloadArtifactoryFileToLocal(artifactoryType: String, path: String, saveDir: String): Result<List<String>> {
        val getFileUrlResult = getArtifactoryFileUrl(artifactoryType, path)
        logger.info("the getFileUrlResult is:{}", JsonUtil.toJson(getFileUrlResult))
        val srcUrlList: List<String>?
        if (0 != getFileUrlResult.status) {
            return Result(getFileUrlResult.status, getFileUrlResult.message)
        } else {
            srcUrlList = getFileUrlResult.data
        }
        val saveFilePathList = ArrayList<String>()
        if (null != srcUrlList) {
            val saveDirFile = File(saveDir)
            if (!saveDirFile.exists()) {
                saveDirFile.mkdirs()
            }
            for (srcUrl in srcUrlList) {
                val lastItem = srcUrl.split("/").last()
                val fileName = lastItem.substring(0, lastItem.indexOf("?"))
                val saveFilePath = "$saveDir/$fileName"
                logger.info("the saveFilePath is:{}", saveFilePath)
                downloadFileToLocal(srcUrl, saveFilePath)
                saveFilePathList.add(saveFilePath)
            }
        }
        logger.info("downloadArtifactoryFileToLocal saveFilePathList is:{}", JsonUtil.toJson(saveFilePathList))
        return Result(saveFilePathList)
    }

    /**
     * 下载多个构建文件到本地
     * @param artifactoryType 版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path 路径
     * @return 文件在本地的保存路径
     */
    fun downloadAllFileToLocal(artifactoryType: String, path: String): Result<List<File>> {
        return downloadAllFileToLocal(artifactoryType, path, SdkUtils.getDataDir())
    }

    /**
     * 下载多个构建文件到本地
     * @param artifactoryType 版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path 路径
     * @param saveDir 文件保存目录
     * @return 文件在本地的保存路径
     */
    fun downloadAllFileToLocal(artifactoryType: String, path: String, saveDir: String): Result<List<File>> {
        val getFileUrlResult = getArtifactoryFileUrl(artifactoryType, path)
        logger.info("the getFileUrlResult is:{}", JsonUtil.toJson(getFileUrlResult))
        if (0 != getFileUrlResult.status) {
            return Result(getFileUrlResult.status, getFileUrlResult.message)
        }
        val saveDirFile = File(saveDir)
        if (!saveDirFile.exists()) {
            saveDirFile.mkdirs()
        }
        return Result(getFileUrlResult.data!!.map {
            val lastItem = it.split("/").last()
            val fileName = lastItem.substring(0, lastItem.indexOf("?token="))
            val saveFilePath = "$saveDir/$fileName"
            logger.info("the saveFilePath is:{}", saveFilePath)
            downloadFileToLocal(it, saveFilePath)
        })
    }

    private fun downloadFileToLocal(srcUrl: String, saveFilePath: String): File {
        try {
            val netUrl = URL(srcUrl)
            val conn = netUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5 * 1000
            // 把文件下载到saveFilePath目录下面
            conn.inputStream.use { inputStream ->
                BufferedOutputStream(FileOutputStream(saveFilePath), 8192).use { outputStream ->
                    IOUtils.copy(inputStream, outputStream, 8192)
                }
            }
        } catch (e: Exception) {
            logger.error("download all files to local error!", e)
        }
        return File(saveFilePath)
    }

    /**
     * 获取所有的文件和文件夹
     */
    fun getAllFiles(isCustom: Boolean): JfrogFilesData {

        val cusListFilesUrl = SdkEnv.genUrl("/jfrog/api/buildAgent/custom/?list&deep=1&listFolders=1")
        val listFilesUrl = SdkEnv.genUrl("/jfrog/api/buildAgent/archive/?list&deep=1&listFolders=1")

        val url = if (!isCustom) listFilesUrl else cusListFilesUrl

        val request = Request.Builder().url(url).get().build()

        // 获取所有的文件和文件夹
        OkhttpUtils.doHttp(request).use { response ->
            val responseBody = response.body()!!.string()
            if (!response.isSuccessful) {
                logger.error("get jfrog files fail:\n $responseBody")
                throw RuntimeException("构建分发获取文件失败")
            }
            try {
                return JsonUtil.fromJson(responseBody, JfrogFilesData::class.java)
            } catch (e: Exception) {
                logger.error("get jfrog files fail\n$responseBody")
                throw RuntimeException("构建分发获取文件失败")
            }
        }
    }

    private fun getUrlHost(url: String): String {
        val p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+(:[0-9]{1,5})?")
        val matcher = p.matcher(url)
        var host = ""
        if (matcher.find()) {
            host = matcher.group()
        }
        return host
    }

    companion object {

        private val logger = LoggerFactory.getLogger(ArtifactoryApi::class.java)
    }
}