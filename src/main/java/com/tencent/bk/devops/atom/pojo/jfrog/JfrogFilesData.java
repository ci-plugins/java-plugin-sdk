package com.tencent.bk.devops.atom.pojo.jfrog;


import lombok.Data;

import java.util.List;
@Data
public class JfrogFilesData {

    public String uri;
    public String created;
    public List<JfrogFile> files;
}
