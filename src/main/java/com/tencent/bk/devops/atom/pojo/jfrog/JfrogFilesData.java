package com.tencent.bk.devops.atom.pojo.jfrog;

import java.util.List;
import lombok.Data;

@Data
public class JfrogFilesData {

  public String uri;
  public String created;
  public List<JfrogFile> files;
}
