package com.tencent.bk.devops.atom.pojo.artifactory;

import lombok.Data;

@Data
public class FileChecksums {

    private  String sha256;/* compiled code */

    private  String sha1;/* compiled code */

    private  String md5;/* compiled code */

}
