package com.tencent.bk.devops.atom.pojo;

import com.tencent.bk.devops.atom.common.DataType;
import lombok.Getter;
import lombok.Setter;

/**
 * "out_var_3": {
 * "type": "report",
 * "label": "",  # 报告别名，用于产出物报告界面标识当前报告
 * "path": "",   # 报告目录所在路径，相对于工作空间
 * "target": "", # 报告入口文件
 * }
 *
 * @version 1.0
 */
@Getter
@Setter
public class ReportData extends DataField {

    private String label;
    private String path;
    private String target;

    public ReportData(String label, String path, String target) {
        super(DataType.report);
        this.label = label;
        this.path = path;
        this.target = target;
    }
}
