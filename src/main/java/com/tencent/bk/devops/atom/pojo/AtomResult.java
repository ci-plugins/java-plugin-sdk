package com.tencent.bk.devops.atom.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.tencent.bk.devops.atom.common.Status;
import com.tencent.bk.devops.atom.pojo.quality.QualityValue;
import com.tencent.bk.devops.atom.utils.I18nUtil;
import com.tencent.bk.devops.atom.utils.MessageUtil;
import com.tencent.bk.devops.plugin.pojo.ErrorType;
import lombok.Data;

import java.util.Map;

/**
 * 流水线插件输出结果
 * {
 * "status": "",     # 插件执行结果，值可以为success、failure、error
 * "message": "",    # 插件执行结果说明，支持markdown格式
 * "type": "default",# 模板类型，目前仅支持default,用于规定data的解析入库方式
 * "data":{          # default模板的数据格式如下：
 * "out_var_1": {
 * "type": "string",
 * "value": "testaaaaa"
 * },
 * "out_var_2": {
 * "type": "artifact",
 * "value": ["file_path_1", "file_path_2"] # 本地文件路径，指定后，agent自动将这些文件归档到仓库
 * },
 * "out_var_3": {
 * "type": "report",
 * "label": "",  # 报告别名，用于产出物报告界面标识当前报告
 * "path": "",   # 报告目录所在路径，相对于工作空间
 * "target": "", # 报告入口文件
 * }
 * }
 * }
 *
 * @version 1.0
 */
@Data
public class AtomResult {

    /**
     * 执行结果
     */
    @JsonProperty("status")
    private Status status = Status.success;

    /**
     * 错误信息
     */
    @JsonProperty("message")
    private String message;

    /**
     * 类型，默认default
     */
    @JsonProperty("type")
    private String type = "default";

    /**
     * 返回字段
     */
    @JsonProperty("data")
    private Map<String, DataField> data = Maps.newHashMap();

    /**
     * 质量红线生成的数据
     */
    @JsonProperty("qualityData")
    private Map<String, QualityValue> qualityData = Maps.newHashMap();

    /**
     * 用于后台度量的错误码
     */
    @JsonProperty("errorType")
    private Integer errorType;

    /**
     * 用于后台度量的错误码
     */
    @JsonProperty("errorCode")
    private Integer errorCode;

    /**
     * 监控数据
     */
    @JsonProperty("monitorData")
    private MonitorData monitorData;


    /**
     * 设置错误信息
     *
     * @param status    执行结果状态
     * @param errorCode 错误码
     * @param errorType 错误类型
     * @param params    替换错误描述信息占位符的参数数组
     */
    public void setErrorInfo(Status status, Integer errorCode, ErrorType errorType, String[] params) {
        this.status = status;
        this.errorCode = errorCode;
        this.errorType = errorType.getNum();
        this.message = MessageUtil.getMessageByLocale(errorCode.toString(), I18nUtil.getLanguage(), params);
    }
}
