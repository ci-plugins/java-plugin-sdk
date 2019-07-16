package com.tencent.bk.devops.plugin.pojo.notify

import com.tencent.bk.devops.atom.pojo.notify.EnumNotifyPriority
import com.tencent.bk.devops.atom.pojo.notify.EnumNotifySource


open class EmailNotifyMessage : BaseMessage() {


    var format: EnumEmailFormat = EnumEmailFormat.PLAIN_TEXT
    var type:   EnumEmailType     = EnumEmailType.OUTER_MAIL
    private val receivers: MutableSet<String> = mutableSetOf()
    private val cc: MutableSet<String> = mutableSetOf()
    private val bcc: MutableSet<String> = mutableSetOf()
    var body: String = ""
    var sender: String = "DevOps"
    var title: String = ""
    var priority: EnumNotifyPriority = EnumNotifyPriority.LOW
    var source: EnumNotifySource = EnumNotifySource.BUSINESS_LOGIC

    fun addReceiver(receiver: String) {
        receivers.add(receiver)
    }

    fun addAllReceivers(receiverSet: Set<String>) {
        receivers.addAll(receiverSet)
    }

    fun addCc(ccSingle: String) {
        cc.add(ccSingle)
    }

    fun addAllCcs(ccSet: Set<String>) {
        cc.addAll(ccSet)
    }

    fun addBcc(bccSingle: String) {
        bcc.add(bccSingle)
    }

    fun addAllBccs(bccSet: Set<String>) {
        bcc.addAll(bccSet)
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

    fun clearBcc() {
        bcc.clear()
    }

    fun getBcc(): Set<String> {
        return bcc.toSet()
    }

    fun clearCc() {
        cc.clear()
    }

    fun getCc(): Set<String> {
        return cc.toSet()
    }

    override fun toString(): String {
        return String.format("title(%s), sender(%s), receivers(%s), cc(%s), bcc(%s), body(email html do not show) ",
                title, sender, receivers, cc, bcc/*, body*/)
    }
}
