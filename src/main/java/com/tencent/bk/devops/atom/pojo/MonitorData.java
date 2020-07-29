package com.tencent.bk.devops.atom.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

@Data
public class MonitorData {

    /**
     * 渠道来源
     */
    @JsonProperty("channel")
    private String channel;

    /**
     * 插件运行开始时间(格式：yyyy-MM-dd HH:mm:ss)
     */
    @JsonProperty("startTime")
    private String startTime;

    /**
     * 插件运行结束时间(格式：yyyy-MM-dd HH:mm:ss)
     */
    @JsonProperty("endTime")
    private String endTime;

    /**
     * 扩展数据
     */
    @JsonProperty("extData")
    private Map<String, Object> extData = Maps.newHashMap();
}
