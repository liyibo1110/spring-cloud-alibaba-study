package com.github.liyibo1110.alibaba.cloud.nacos;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 检查spring.cloud.nacos.discovery.enabled属性是为为true，默认不写也是true
 * @author liyibo
 * @date 2026-06-14 13:25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ConditionalOnProperty(value = "spring.cloud.nacos.discovery.enabled", matchIfMissing = true)
public @interface ConditionalOnNacosDiscoveryEnabled {

}
