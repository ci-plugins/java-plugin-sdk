package com.tencent.bk.devops.plugin.api.impl

import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.plugin.api.ArchiveApi
import org.slf4j.LoggerFactory
import java.io.File

class ArchiveGetResourceApi : BaseApi() {
    private val archiveApi: ArchiveApi = ArchiveApi()

    /**
     * 根据jfrogFile中的uri下载自定义仓库文件到指定目录
     * @param uri  对应JfrogFile文件中的uri
     * @param destPath 要下载到的文件地址
     * @return
     */
    fun downloadCustomizeFile(uri: String, destPath: File ,size:Long,bkWorkSpace:String){
        val url = "/jfrog/storage/build/custom$uri"
        val request = buildGet(url)
        archiveApi.download(uri,request, destPath,size,bkWorkSpace)
    }

    /**
     * 根据jfrogFile中的uri下载流水线仓库文件到指定目录
     * @param pipelineId  要下载流水线的pipelineId
     * @param buildId  要下载流水线对应的构建idbuildId
     * @param uri  对应JfrogFile文件中的uri
     * @param destPath 要下载到的文件地址
     * @return
     */
    fun downloadPipelineFile(pipelineId: String, buildId: String, uri: String, destPath: File, size:Long,bkWorkSpace:String) {
        val url = "/jfrog/storage/build/archive/$pipelineId/$buildId$uri"
        val request = buildGet(url)
        archiveApi.download(uri,request, destPath,size,bkWorkSpace)
    }


}