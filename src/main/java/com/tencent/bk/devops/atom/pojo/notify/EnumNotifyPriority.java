package com.tencent.bk.devops.atom.pojo.notify;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumNotifyPriority {
  LOW("-1"),
  NORMAL("0"),
  HIGH("1");
  private String property;

  EnumNotifyPriority(String property) {
    this.property = property;
  }

  @JsonValue
  public String getProperty() {
    return property;
  }
}
