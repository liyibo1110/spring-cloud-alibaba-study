package com.github.liyibo1110.alibaba.cloud.nacos.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于把指定dataId/group/key的配置注入到字段或Bean
 * @author liyibo
 * @date 2026-06-06 16:13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface NacosConfig {
    String group();
    String dataId();
    String key() default "";
    String defaultValue() default "";
    boolean refreshed() default true;
}
