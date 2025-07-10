package com.tencent.bk.devops.atom.pojo;

import com.tencent.bk.devops.atom.common.DataType;
import lombok.Getter;
import lombok.Setter;

/**
 * 插件字段抽象类
 * @version 1.0
 */
@Setter
@Getter
@SuppressWarnings("all")
public abstract class DataField {

    public DataField(DataType type) {
        this.type = type;
    }


    public DataField(DataType type, boolean isSensitive) {
        this.type = type;
        this.isSensitive = isSensitive;
    }

    /**
     * 类型
     */
    private DataType type;


    /**
     * 是否属于敏感字段
     */
    private boolean isSensitive = false;
}
