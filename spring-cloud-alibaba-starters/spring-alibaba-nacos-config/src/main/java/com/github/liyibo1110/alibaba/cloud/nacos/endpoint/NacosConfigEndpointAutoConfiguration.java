package com.github.liyibo1110.alibaba.cloud.nacos.endpoint;

import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigEnabledCondition;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigManager;
import com.github.liyibo1110.alibaba.cloud.nacos.refresh.NacosRefreshHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * Endpoint相关的自动装配，生成NacosConfigEndpoint和NacosConfigHealthIndicator这两个组件。
 * @author liyibo
 * @date 2026-06-04 10:51
 */
@ConditionalOnWebApplication
@ConditionalOnClass(Endpoint.class)
@Conditional(NacosConfigEnabledCondition.class)
public class NacosConfigEndpointAutoConfiguration {

    @Autowired
    private NacosConfigManager nacosConfigManager;

    @Autowired
    private NacosRefreshHistory nacosRefreshHistory;

    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    @Bean
    public NacosConfigEndpoint nacosConfigEndpoint() {
        return new NacosConfigEndpoint(nacosConfigManager.getNacosConfigProperties(), nacosRefreshHistory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "spring.nacos.config.health-indicator.enabled", havingValue = "true", matchIfMissing = false)
    @ConditionalOnEnabledHealthIndicator("nacos-config")
    public NacosConfigHealthIndicator nacosConfigHealthIndicator() {
        return new NacosConfigHealthIndicator(nacosConfigManager.getConfigService());
    }
}
