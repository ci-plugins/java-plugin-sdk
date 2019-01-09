package com.tencent.bk.devops.atom.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 流水线原子基础参数, 所有原子参数继承扩展他增加自己的定义
 *
 * @version 1.0
 */
@Getter
@Setter
public class AtomBaseParam {

    /**
     * 流水线版本号
     */
    @JsonProperty("pipeline.version")
    private String pipelineVersion;

    /**
     * 流水线构建序号
     */
    @JsonProperty("pipeline.build.num")
    private String pipelineBuildNum;
    /**
     * 流水线名称
     */
    @JsonProperty("pipeline.name")
    private String pipelineName;
    /**
     * 流水线启动时间：毫秒
     */
    @JsonProperty("pipeline.time.start")
    private String pipelineStartTimeMills;

    /**
     * 流水线触发人
     */
    @JsonProperty("pipeline.start.user.name")
    private String pipelineStartUserName;

}
