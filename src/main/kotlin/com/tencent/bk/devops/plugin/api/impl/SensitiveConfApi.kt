package com.tencent.bk.devops.plugin.api.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.bk.devops.atom.api.BaseApi
import com.tencent.bk.devops.plugin.pojo.Result
import com.tencent.bk.devops.plugin.pojo.atom.SensitiveConfResp
import com.tencent.bk.devops.plugin.utils.JsonUtil

class SensitiveConfApi : BaseApi() {

    /**
     * 获取插件私有配置
     * @param atomCode 插件代码
     * @return 插件私有配置列表
     */
    fun getAtomSensitiveConf(atomCode: String): Result<List<SensitiveConfResp>?> {
        val path = "/store/api/build/store/sensitiveConf/types/ATOM/codes/$atomCode"
        val request = buildGet(path)
        val responseContent = retryRequest(request, "获取插件私有配置失败")
        return JsonUtil.to(responseContent, object : TypeReference<Result<List<SensitiveConfResp>?>>() {})
    }
}
