package com.tencent.bk.devops.atom.pojo.artifactory;


import lombok.Data;
import java.util.Map;

@Data
public class FileDetail {

    private  String name;/* compiled code */

    private  String path; /* compiled code */

    private  String fullName;/* compiled code */

    private  String fullPath; /* compiled code */

    private  Long size; /* compiled code */

    private  Long createdTime; /* compiled code */

    private  Long modifiedTime; /* compiled code */

    private  FileChecksums checksums; /* compiled code */

    private  Map<String, String> meta;/* compiled code */
}
