package com.tencent.bk.devops.atom.notify;

import com.tencent.bk.devops.plugin.pojo.notify.BaseMessage;
import lombok.Data;

import java.util.Set;

@Data
public class RtxMessage extends BaseMessage {
    private String sender;
    private String title;
    private String body;
    private Set<String > receivers;
    private EnumNotifyPriority priority;
    private EnumNotifySource source;
}
