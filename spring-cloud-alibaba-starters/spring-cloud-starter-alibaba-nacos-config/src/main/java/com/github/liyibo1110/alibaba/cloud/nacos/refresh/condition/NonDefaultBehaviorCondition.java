package com.github.liyibo1110.alibaba.cloud.nacos.refresh.condition;

import com.alibaba.cloud.nacos.NacosPropertiesPrefixer;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author liyibo
 * @date 2026-06-09 11:20
 */
public class NonDefaultBehaviorCondition extends SpringBootCondition {
    private static final RefreshBehavior DEFAULT_REFRESH_BEHAVIOR = RefreshBehavior.ALL_BEANS;

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        String prefix = NacosPropertiesPrefixer.getPrefix(context.getEnvironment());
        RefreshBehavior behavior = context.getEnvironment().getProperty(
                prefix + ".config.refresh-behavior", RefreshBehavior.class,
                DEFAULT_REFRESH_BEHAVIOR);
        if (DEFAULT_REFRESH_BEHAVIOR == behavior) {
            return ConditionOutcome.noMatch("no matched");
        }
        return ConditionOutcome.match("matched");
    }
}
