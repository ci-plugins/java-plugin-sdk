package com.tencent.bk.devops.plugin.exception.docker

import java.lang.RuntimeException

data class DockerPullException(
    val errorMsg: String,
    val errorCode: Int = 2198001
): RuntimeException(errorMsg)