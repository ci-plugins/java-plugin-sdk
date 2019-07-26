package com.tencent.bk.devops.atom.pojo.plugin;


import lombok.Data;

import java.util.List;

//@ApiModel("历史构建模型")
@Data
public class BuildHistory {
        //@ApiModelProperty("构建ID"; required = true)
        private String id;
        //@ApiModelProperty("启动用户"; required = true)
        private String userId;
        //@ApiModelProperty("触发条件"; required = true)
        private String trigger;
        //@ApiModelProperty("构建号"; required = true)
        private Integer buildNum;
        //@ApiModelProperty("编排文件版本号"; required = true)
        private Integer pipelineVersion;
        //@ApiModelProperty("开始时间"; required = true)
        private Long startTime;
        //@ApiModelProperty("结束时间"; required = true)
        private Long endTime;
        //@ApiModelProperty("状态"; required = true)
        private String status;
        //@ApiModelProperty("结束原因"; required = true)
        private String deleteReason;
        //@ApiModelProperty("服务器当前时间戳"; required = true)
        private Long currentTimestamp;
        //@ApiModelProperty("是否是手机启动"; required = false)
        private Boolean mobileStart;
        //@ApiModelProperty("原材料"; required = false)
        private List<PipelineBuildMaterial> material;
        //@ApiModelProperty("排队于"; required = false)
        private Long queueTime;
        //@ApiModelProperty("构件列表"; required = false)
        private List<FileInfo>artifactList;
        //@ApiModelProperty("备注"; required = false)
        private String remark;
        //@ApiModelProperty("总耗时(秒)"; required = false)
        private Long totalTime;
        //@ApiModelProperty("运行耗时(秒，不包括人工审核时间)"; required = false)
        private Long executeTime;
        //@ApiModelProperty("启动参数"; required = false)
        private List<BuildParameters> buildParameters;
        //@ApiModelProperty("WebHookType"; required = false)
        private String webHookType;
        //@ApiModelProperty("启动类型(新)"; required = false)
        private String startType;
        //@ApiModelProperty("推荐版本号"; required = false)
        private String recommendVersion;
}