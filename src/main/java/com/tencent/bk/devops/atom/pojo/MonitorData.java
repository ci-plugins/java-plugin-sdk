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
     * 插件运行开始时间(格式：时间戳)
     */
    @JsonProperty("startTime")
    private long startTime;

    /**
     * 插件运行结束时间(格式：时间戳)
     */
    @JsonProperty("endTime")
    private long endTime;

    /**
     * 扩展数据
     */
    @JsonProperty("extData")
    private Map<String, Object> extData = Maps.newHashMap();
}
