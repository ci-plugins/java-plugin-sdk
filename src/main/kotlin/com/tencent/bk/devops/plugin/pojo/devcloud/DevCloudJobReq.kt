package com.tencent.bk.devops.plugin.pojo.devcloud

import com.tencent.bk.devops.plugin.docker.pojo.job.request.JobParam
import com.tencent.bk.devops.plugin.docker.pojo.job.request.Registry

data class DevCloudJobReq(
    @JvmField var alias: String? = null,
    @JvmField var activeDeadlineSeconds: Int? = null,
    @JvmField var image: String? = null,
    @JvmField var registry: Registry? = null,
    @JvmField var params: JobParam? = null,
    @JvmField var podNameSelector: String? = null,
    @JvmField var mountPath: String? = null
)
