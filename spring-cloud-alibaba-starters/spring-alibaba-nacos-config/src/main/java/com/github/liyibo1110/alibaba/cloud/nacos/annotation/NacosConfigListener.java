package com.github.liyibo1110.alibaba.cloud.nacos.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于监听某个dataId/group的完整配置变更，当配置变化后，把最新配置内容转换成方法参数并回调。
 * @author liyibo
 * @date 2026-06-06 16:16
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface NacosConfigListener {
    String group();
    String dataId();
    String key();
    boolean initNotify() default false;
}
