package com.tencent.bk.devops.plugin.pojo.notify


data class Receiver(
    val type: ReceiverType = ReceiverType.single,
    val id: String = ""
)
