package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.atom.api.SdkEnv
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

class ArtifactoryApi : BaseApi() {

    /**
     * 获取构建文件下载路径
     * @param artifactoryType  版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path 路径 多路径使用","或者";"分割
     * @return 文件下载路径数组
     */
    fun getArtifactoryFileUrl(artifactoryType: String, path: String): Result<List<String>> {
        val urlBuilder = StringBuilder("/artifactory/api/build/artifactories/thirdPartyDownloadUrl?artifactoryType=")
        urlBuilder.append(artifactoryType).append("&path=").append(path).append("&ttl=").append(3600) //下载链接有效期设定为1小时
        val requestUrl = urlBuilder.toString()
        logger.info("the requestUrl is:{}", requestUrl)
        val request = super.buildGet(urlBuilder.toString())
        var responseContent: String? = null
        try {
            responseContent = super.request(request, "获取包路径失败")
        } catch (e: IOException) {
            logger.error("get artifactoryFileUrl throw Exception", e)
        }

        logger.info("getArtifactoryFileUrl responseContent is:{}", responseContent)
        return if (null != responseContent) {
            JsonUtil.fromJson(responseContent, object : TypeReference<Result<List<String>>>() {

            })
        } else {
            Result(emptyList())
        }
    }

    /**
     * 下载构建文件到本地
     * @param artifactoryType  版本仓库类型 PIPELINE：流水线，CUSTOM_DIR：自定义
     * @param path 路径
     * @return 文件在本地的保存路径
     */
    fun downloadArtifactoryFileToLocal(artifactoryType: String, path: String): Result<String> {
        val getFileUrlResult = getArtifactoryFileUrl(artifactoryType, path)
        logger.info("the getFileUrlResult is:{}", JsonUtil.toJson(getFileUrlResult))
        var srcUrl: String? = null
        if (0 != getFileUrlResult.status) {
            return Result(getFileUrlResult.status, getFileUrlResult.message)
        } else {
            val data = getFileUrlResult.data
            if (null != data && data.isNotEmpty()) {
                srcUrl = data[0]
            }
        }
        val arrays = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var fileName = arrays[arrays.size - 1]
        val index = fileName.indexOf("?")
        if (-1 != index) {
            fileName = fileName.substring(0, index)
        }
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        val dataDir = SdkUtils.getDataDir()
        logger.info("the dataDir is:{}", dataDir)
        val saveFilePath = "$dataDir/$fileName"
        try {
            val netUrl = URL(srcUrl!!)
            val conn = netUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5 * 1000
            inputStream = conn.inputStream
            //把文件下载到dataDir目录下面
            outputStream = BufferedOutputStream(FileOutputStream(saveFilePath), 8192)
            IOUtils.copy(inputStream, outputStream, 8192)
        } catch (e: IOException) {
            logger.error("downloadArtifactoryFileToLocal error!", e)
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    logger.error("outputStream close error!", e)
                } finally {
                    if (null != inputStream) {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            logger.error("inputStream close error!", e)
                        }

                    }
                }
            }
        }
        return Result(saveFilePath)
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

    companion object {

        private val logger = LoggerFactory.getLogger(ArtifactoryApi::class.java)
    }


}
