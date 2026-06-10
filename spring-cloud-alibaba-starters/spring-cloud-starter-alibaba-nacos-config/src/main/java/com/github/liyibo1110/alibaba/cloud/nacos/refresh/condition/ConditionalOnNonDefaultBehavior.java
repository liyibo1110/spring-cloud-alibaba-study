package com.github.liyibo1110.alibaba.cloud.nacos.refresh.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liyibo
 * @date 2026-06-09 11:19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(com.github.liyibo1110.alibaba.cloud.nacos.refresh.condition.NonDefaultBehaviorCondition.class)
public @interface ConditionalOnNonDefaultBehavior {

}
