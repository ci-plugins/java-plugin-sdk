package com.tencent.bk.devops.plugin.docker.exception

import java.lang.RuntimeException

data class DockerRunException(
    val errorMsg: String,
    val errorCode: Int = 2198002
) : RuntimeException(errorMsg)
