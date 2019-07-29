package com.tencent.bk.devops.plugin.pojo.artifactory


//("渠道代码")
enum class ChannelCode {
    //("蓝盾")
    BS,
    //("原子市场")
    AM,
    //("CodeCC")
    CODECC,
    //("GCloud")
    GCLOUD;

    companion object {
        // Only BS need to check the authentication for now
        fun isNeedAuth(channelCode: ChannelCode) =
                channelCode == BS
    }
}