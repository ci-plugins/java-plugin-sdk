package com.tencent.bk.devops.plugin.pojo.notify

import com.tencent.bk.devops.atom.notify.EnumNotifyPriority
import com.tencent.bk.devops.atom.notify.EnumNotifySource


open class SmsNotifyMessage : BaseMessage() {

    private val receivers: MutableSet<String> = mutableSetOf()
    var body: String = ""
    var sender: String = ""
    var priority: EnumNotifyPriority = EnumNotifyPriority.LOW
    var source: EnumNotifySource = EnumNotifySource.BUSINESS_LOGIC

    fun addReceiver(receiver: String) {
        receivers.add(receiver)
    }

    fun addAllReceivers(receiverSet: Set<String>) {
        receivers.addAll(receiverSet)
    }

    fun clearReceivers() {
        receivers.clear()
    }

    fun getReceivers(): Set<String> {
        return receivers.toSet()
    }

    fun isReceiversEmpty(): Boolean {
        if (receivers.size == 0) return true
        return false
    }

    override fun toString(): String {
        return String.format("sender(%s), receivers(%s), body(%s) ",
                sender, receivers, body)
    }
}