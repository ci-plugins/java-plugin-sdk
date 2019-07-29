package com.tencent.bk.devops.atom.pojo.artifactory;


import lombok.Data;

@Data
public class PipelineBuildMaterial {
    private String aliasName;
    private String branchName;
    private Integer commitTimes;
    private String newCommitComment;
    private String newCommitId;
    private String url;
}
