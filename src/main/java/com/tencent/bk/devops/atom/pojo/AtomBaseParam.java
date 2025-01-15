package com.tencent.bk.devops.atom.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * 流水线插件基础参数, 所有插件参数继承扩展他增加自己的定义
 *
 * @version 1.0
 */
@Getter
@Setter
public class AtomBaseParam {

    /** 工作空间 */
    @JsonProperty("bkWorkspace")
    private String bkWorkspace;

    /** 是否是测试版本 Y：是 N：否 */
    @JsonProperty("testVersionFlag")
    private String testVersionFlag;

    /** 流水线版本号 */
    @JsonProperty("BK_CI_PIPELINE_VERSION")
    private String pipelineVersion;

    /** 项目名称 */
    @JsonProperty("BK_CI_PROJECT_NAME")
    private String projectName;

    /** 项目中文名称 */
    @JsonProperty("BK_CI_PROJECT_NAME_CN")
    private String projectNameCn;

    /** 流水线Id */
    @JsonProperty("BK_CI_PIPELINE_ID")
    private String pipelineId;

    /** 流水线构建序号 */
    @JsonProperty("BK_CI_BUILD_NUM")
    private String pipelineBuildNum;

    /** 流水线构建Id */
    @JsonProperty("BK_CI_BUILD_ID")
    private String pipelineBuildId;

    /** 流水线名称 */
    @JsonProperty("BK_CI_PIPELINE_NAME")
    private String pipelineName;

    /** 流水线启动时间：毫秒 */
    @JsonProperty("BK_CI_BUILD_START_TIME")
    private String pipelineStartTimeMills;

    /** 流水线执行人 */
    @JsonProperty("BK_CI_START_USER_ID")
    private String pipelineStartUserId;

    /** 流水线触发人 */
    @JsonProperty("BK_CI_START_USER_NAME")
    private String pipelineStartUserName;

    /** 流水线创建人 */
    @JsonProperty("BK_CI_PIPELINE_CREATE_USER")
    private String pipelineCreateUserName;

    /** 流水线修改人 */
    @JsonProperty("BK_CI_PIPELINE_UPDATE_USER")
    private String pipelineUpdateUserName;

    /** 插件敏感信息 */
    @JsonProperty(value = "bkSensitiveConfInfo", access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, String> bkSensitiveConfInfo;

    /** 流水线当前插件id */
    @JsonProperty("BK_CI_BUILD_TASK_ID")
    private String pipelineTaskId;

    /** 流水线当前插件标识 */
    @JsonProperty("BK_CI_ATOM_CODE")
    private String atomCode;

    /** 流水线当前插件名称 */
    @JsonProperty("BK_CI_ATOM_NAME")
    private String atomName;

    /** 流水线当前插件版本 */
    @JsonProperty("BK_CI_ATOM_VERSION")
    private String version;

    /** 流水线当前插件任务名称（步骤名称） */
    @JsonProperty("BK_CI_TASK_NAME")
    private String taskName;

    /** 流水线当前插件自定义ID（上下文标识） */
    @JsonProperty("BK_CI_STEP_ID")
    private String stepId;

    /** 插件后置动作标识 */
    private String postEntryParam = System.getProperty("postEntryParam");

    /** 插件后置动作父任务id */
    private String parentTaskId;
}
