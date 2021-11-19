package com.tencent.bk.devops.plugin.docker.pojo.common

object KubernetesPodStatus {
    // 准备中
    const val pending = "pending"

    // 运行中
    const val running = "running"

    // 成功
    const val successed = "successed"

    // 失败
    const val failed = "failed"
}
