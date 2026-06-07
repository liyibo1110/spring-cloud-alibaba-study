package com.github.liyibo1110.alibaba.cloud.nacos.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于监听某个dataId/group中指定key或key前缀的变化，当变化时回调方法，并传入变更明细。
 * @author liyibo
 * @date 2026-06-06 16:17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface NacosConfigKeysListener {
    String group();
    String dataId();
    String[] interestedKeys() default {};
    String[] interestedKeyPrefixes() default {};
}
