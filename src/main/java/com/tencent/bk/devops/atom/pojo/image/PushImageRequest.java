package com.tencent.bk.devops.atom.pojo.image;

import lombok.Data;

@Data
public class PushImageRequest {

    private String userId; // 操作人
    private String srcImageName; // 源镜像名称
    private String srcImageTag; // 源镜像tag
    private String repoAddress; // 目标镜像仓库地址
    private String namespace; // 目标命名空间
    private String targetImageName; // 目标镜像名称
    private String targetImageTag; // 目标镜像tag
    private String ticketId; // 蓝盾凭证服务中的凭证id

    public PushImageRequest() {
    }

    public PushImageRequest(
        String userId,
        String srcImageName,
        String srcImageTag,
        String repoAddress,
        String namespace,
        String targetImageName,
        String targetImageTag
    ) {
        this.userId = userId;
        this.srcImageName = srcImageName;
        this.srcImageTag = srcImageTag;
        this.repoAddress = repoAddress;
        this.namespace = namespace;
        this.targetImageName = targetImageName;
        this.targetImageTag = targetImageTag;
    }

    public PushImageRequest(
        String userId,
        String srcImageName,
        String srcImageTag,
        String repoAddress,
        String namespace,
        String targetImageName,
        String targetImageTag,
        String ticketId
    ) {
        this.userId = userId;
        this.srcImageName = srcImageName;
        this.srcImageTag = srcImageTag;
        this.repoAddress = repoAddress;
        this.namespace = namespace;
        this.targetImageName = targetImageName;
        this.targetImageTag = targetImageTag;
        this.ticketId = ticketId;
    }


}
