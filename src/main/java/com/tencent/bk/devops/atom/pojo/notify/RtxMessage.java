package com.tencent.bk.devops.atom.pojo.notify;

import com.tencent.bk.devops.plugin.pojo.notify.BaseMessage;
import java.util.Set;
import lombok.Data;

@Data
public class RtxMessage extends BaseMessage {
  private String sender;
  private String title;
  private String body;
  private Set<String> receivers;
  private EnumNotifyPriority priority;
  private EnumNotifySource source;
}
