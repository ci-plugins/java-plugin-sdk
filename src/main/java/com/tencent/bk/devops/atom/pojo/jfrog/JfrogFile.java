package com.tencent.bk.devops.atom.pojo.jfrog;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JfrogFile {
  public String uri;
  public Long size;
  public String lastModified;
  public Boolean folder;

  @JsonProperty(required = false)
  public String sha1;
}
