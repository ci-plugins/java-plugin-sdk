package com.tencent.bk.devops.plugin.api

import com.tencent.bk.devops.plugin.pojo.cos.CdnUploadFileInfo
import com.tencent.bk.devops.plugin.pojo.cos.SpmFile
import com.tencent.bk.devops.plugin.pojo.Result
/**
 * 分发文件至CDN
 * @param projectId            项目ID
 * @param pipelineId           流水线ID
 * @param buildId              构建ID
 * @param elementId            原子ID
 * @param executeCount         执行次数
 * @param cdnUploadFileInfo    文件信息
 */
interface UploadFileToCosApi {
    fun uploadCdn(
            projectId: String,
            pipelineId: String,
            buildId: String,
            elementId: String,
            executeCount: Int,
            cdnUploadFileInfo: CdnUploadFileInfo
    ): Result<SpmFile>
}