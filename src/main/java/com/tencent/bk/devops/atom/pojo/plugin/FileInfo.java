package com.tencent.bk.devops.atom.pojo.plugin;


import com.tencent.bk.devops.atom.common.ArtifactoryType;
import lombok.Data;

@Data
public class FileInfo {
   
         private  String appVersion;/* compiled code */

         private ArtifactoryType artifactoryType;  /* compiled code */

         private  Boolean folder; /* compiled code */

         private  String fullName;/* compiled code */

         private  String fullPath; /* compiled code */

         private  Long modifiedTime; /* compiled code */

         private  String name; /* compiled code */

         private  String path;/* compiled code */
}
