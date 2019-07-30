package com.tencent.bk.devops.plugin.api.impl



import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import com.tencent.bk.devops.atom.api.impl.CredentialApi
import com.tencent.bk.devops.plugin.api.UploadFileToCosApi
import com.tencent.bk.devops.plugin.api.cos.CosService
import com.tencent.bk.devops.plugin.api.cos.UploadCosCdnParam
import com.tencent.bk.devops.plugin.api.cos.UploadCosCdnThread
import com.tencent.bk.devops.plugin.pojo.cos.CdnUploadFileInfo
import com.tencent.bk.devops.plugin.pojo.cos.SpmFile
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.utils.OkhttpUtils
import com.tencent.devops.common.cos.COSClientConfig
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory


class UploadFileToCosApiImpl constructor(
        private val cosService: CosService
        ) : UploadFileToCosApi {

    private val parser = JsonParser()
    private val credentialApi=CredentialApi()

    override fun uploadCdn(projectId: String, pipelineId: String, buildId: String, elementId: String, executeCount: Int, cdnUploadFileInfo: CdnUploadFileInfo, mapOperation: MutableMap<String, String>): Result<SpmFile> {
        // 根据ticketid从ticketService获取凭证信息
        val ticketsMap =credentialApi.getCredential(cdnUploadFileInfo.ticketId).data
        logger.info("ticketsMap is :$ticketsMap")
        // 根据spm的appId以及secretKey，调用spm接口，获取cos系统的appid，bucket，root_path以及业务外网CDN域名
        val spmAppId = ticketsMap["appId"].toString()
        logger.info("spmAppId is : $spmAppId")
        val spmSecretKey = ticketsMap["secretKey"].toString()
        logger.info("spmSecretKey is :$spmSecretKey")
        val cosAppInfo = getCosAppInfoFromSpm(spmAppId, spmSecretKey)
        val cosClientConfig = COSClientConfig(cosAppInfo.cosAppId, spmAppId, spmSecretKey)
        var cdnPath = if (cdnUploadFileInfo.cdnPathPrefix.startsWith("/")) {
            "/" + cosAppInfo.rootPath + cdnUploadFileInfo.cdnPathPrefix
        } else {
            "/" + cosAppInfo.rootPath + "/" + cdnUploadFileInfo.cdnPathPrefix
        }
        if (!cdnPath.endsWith("/")) {
            cdnPath = "$cdnPath/"
        }
        val uploadTaskKey = "upload_cdn_task_${projectId}_${pipelineId}_${buildId}_$elementId"
        val uploadCosCdnParam = UploadCosCdnParam(
            projectId, pipelineId, buildId, elementId, cdnUploadFileInfo.regexPaths,
            cdnUploadFileInfo.customize, cosAppInfo.bucket, cdnPath, cosAppInfo.domain, cosClientConfig
        )
        val uploadCosCdnThread =
            UploadCosCdnThread(cosService, mapOperation, uploadCosCdnParam)
        val uploadThread = Thread(uploadCosCdnThread, uploadTaskKey)
        logger.info("开始上传CDN...")
        uploadThread.start()
        cdnPath = cosAppInfo.domain + cdnPath
        val spmFile = SpmFile(uploadTaskKey, cdnPath)
        return Result(spmFile)
    }

    private fun getCosAppInfoFromSpm(spmAppId: String, spmSecretKey: String): CosAppInfo {
        val url = "http://spm.oa.com/cdntool/get_bu_info.py"

        val requestData = mapOf(
            "bu_id" to spmAppId,
            "secret_key" to spmSecretKey
        )
        val requestBody = ObjectMapper().writeValueAsString(requestData)
        logger.info("POST url: $url")
        logger.info("requestBody: $requestBody")

        try {
            val request = Request.Builder().url(url).post(RequestBody.create(JSON, requestBody)).build()
            OkhttpUtils.doHttp(request).use { response ->
                val responseBody = response.body()?.string()
                logger.info("responseBody: $responseBody")
                val responseJson = parser.parse(responseBody).asJsonObject
                val code = responseJson["code"].asInt
                if (0 != code) {
                    val msg = responseJson.asJsonObject["msg"]
                    throw RuntimeException("请求SPM失败, msg:$msg")
                }
                val rootPath = responseJson["root_path"].asString
                val domain = responseJson["domain"].asString
                val bucket = responseJson["bucket"].asString
                val appid = responseJson["appid"].asString
                return CosAppInfo(rootPath, domain, bucket, appid.toLong())
            }
        } catch (e: Exception) {
            logger.error("Get cos app info failed", e)
            throw Exception("Get cos app info failed.")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UploadFileToCosApiImpl::class.java)
        private val JSON = MediaType.parse("application/json;charset=utf-8")
    }

    data class CosAppInfo(val rootPath: String, val domain: String, val bucket: String, val cosAppId: Long)
}