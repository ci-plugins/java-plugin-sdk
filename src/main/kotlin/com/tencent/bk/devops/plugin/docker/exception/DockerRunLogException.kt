package com.tencent.bk.devops.plugin.docker.exception

import java.lang.RuntimeException

data class DockerRunLogException(
    val errorMsg: String,
    val errorCode: Int = 2198003
) : RuntimeException(errorMsg)
