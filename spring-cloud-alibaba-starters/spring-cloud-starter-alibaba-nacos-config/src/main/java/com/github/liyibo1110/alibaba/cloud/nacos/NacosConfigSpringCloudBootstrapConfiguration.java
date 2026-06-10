package com.github.liyibo1110.alibaba.cloud.nacos;

import com.alibaba.cloud.nacos.NacosConfigEnabledCondition;
import com.github.liyibo1110.alibaba.cloud.nacos.refresh.SmartConfigurationPropertiesRebinder;
import com.github.liyibo1110.alibaba.cloud.nacos.refresh.condition.ConditionalOnNonDefaultBehavior;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.cloud.context.properties.ConfigurationPropertiesBeans;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-06-09 11:18
 */
@Configuration(proxyBeanMethods = false)
@Conditional(NacosConfigEnabledCondition.class)
public class NacosConfigSpringCloudBootstrapConfiguration {

    @Bean
    @ConditionalOnMissingBean(search = SearchStrategy.CURRENT)
    @ConditionalOnNonDefaultBehavior
    public ConfigurationPropertiesRebinder smartConfigurationPropertiesRebinder(
            ConfigurationPropertiesBeans beans) {
        return new SmartConfigurationPropertiesRebinder(beans);
    }
}
