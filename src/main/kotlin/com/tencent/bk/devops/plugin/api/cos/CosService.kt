package com.tencent.bk.devops.plugin.api.cos

import com.tencent.devops.common.cos.COSClient
import com.tencent.devops.common.cos.COSClientConfig
import com.tencent.devops.common.cos.request.AppendObjectRequest
import com.tencent.devops.common.cos.request.DeleteObjectRequest
import org.slf4j.LoggerFactory

class CosService {

    @Throws(Exception::class)
    fun append(
        cosClientConfig: COSClientConfig,
        bucket: String,
        fileName: String,
        headers: Map<String, String>?,
        content: ByteArray,
        positionAppend: Long,
        contentType: String
    ): Long {
        val cosClient = COSClient(cosClientConfig)

        val appendObjectRequest = AppendObjectRequest(
                bucket,
                fileName,
                headers,
                content,
                positionAppend,
                contentType
        )
        val appendObjectResponse = cosClient.appendObject(appendObjectRequest)

        if (!appendObjectResponse.isSuccess) {
            val errorMessage = String.format("Append file(%s) trunk at position %s to COS failed", fileName, positionAppend)
            logger.error(errorMessage + appendObjectResponse.errorMessage)
            throw RuntimeException(errorMessage)
        }
        return appendObjectResponse.getNextAppendPosition()
    }

    fun deleteFile(cosClientConfig: COSClientConfig, bucket: String, fileName: String): Boolean {
        try {
            val cosClient = COSClient(cosClientConfig)
            val request = DeleteObjectRequest(bucket, fileName)
            val response = cosClient.deleteObject(request)
            if (response.isSuccess) {
                val msg = String.format(
                        "Delete existing file(%s) from COS succeeded", fileName)
                logger.info(msg)
                return true
            }
            if (response.isNotFound) {
                val msg = String.format(
                        "Delete existing file(%s) from COS succeeded: file not originally exists ", fileName)
                logger.info(msg)
                return true
            }
            val msg = String.format("Delete file(%s) from COS failed: %s", fileName, response.getErrorMessage())
            logger.error(msg)
            return false
        } catch (ex: Exception) {
            val msg = String.format("Delete file(%s) from COS  failed: %s", fileName, ex.message)
            logger.error(msg, ex)
            return false
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CosService::class.java)
    }
}