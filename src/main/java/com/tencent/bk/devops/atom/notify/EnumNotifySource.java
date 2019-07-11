package com.tencent.bk.devops.atom.notify;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumNotifySource {
    BUSINESS_LOGIC(0), OPERATION(1);
    private  int value;

    EnumNotifySource(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
