package com.tencent.bk.devops.atom.spi;

import com.tencent.bk.devops.atom.pojo.AtomBaseParam;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AtomService {

    /**
     * 值
     */
    String value() default "";

    /**
     * 排序顺序
     *
     * @return sortNo
     */
    int order() default 0;

    /**
     * 参数类
     */
    Class<? extends AtomBaseParam> paramClass();
}
