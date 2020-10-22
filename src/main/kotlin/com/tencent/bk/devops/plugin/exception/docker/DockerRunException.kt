package com.tencent.bk.devops.plugin.exception.docker

import java.lang.RuntimeException

data class DockerRunException(
    val errorMsg: String,
    val errorCode: Int = 2198002
): RuntimeException(errorMsg)