package com.tencent.bk.devops.atom.pojo.image;

import lombok.Data;

@Data
public class PushImageTask {

    private String taskId; // 异步任务id，可使用此id轮询进度，获得结果
    private String projectId; // 项目ID
    private String operator; // 操作者
    private long createdTime; // 创建时间
    private long updatedTime; // 更新时间
    private String taskStatus; // 任务状态，RUNNING 运行中 FAILED 失败 SUCCESS 成功 TIMEOUT 超时
    private String taskMessage; // 任务详情

}
