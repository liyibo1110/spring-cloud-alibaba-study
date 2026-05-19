package com.github.liyibo1110.alibaba.cloud.nacos.utils;

import com.github.liyibo1110.alibaba.cloud.nacos.NacosPropertiesPrefixer;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 是否开启Nacos Config的Condition。
 * @author liyibo
 * @date 2026-05-19 18:19
 */
public class NacosConfigEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String prefix = NacosPropertiesPrefixer.getPrefix(context.getEnvironment());
        // 没找到配置字段也算true
        return context.getEnvironment().getProperty(prefix + ".config.enabled", Boolean.class, true);
    }
}
