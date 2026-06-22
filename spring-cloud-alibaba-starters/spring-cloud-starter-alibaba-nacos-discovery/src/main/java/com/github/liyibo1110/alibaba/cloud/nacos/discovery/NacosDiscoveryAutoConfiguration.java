package com.github.liyibo1110.alibaba.cloud.nacos.discovery;

import com.github.liyibo1110.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosServiceManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-06-22 11:26
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
public class NacosDiscoveryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NacosDiscoveryProperties nacosProperties() {
        return new NacosDiscoveryProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public NacosServiceDiscovery nacosServiceDiscovery(
            NacosDiscoveryProperties discoveryProperties,
            NacosServiceManager nacosServiceManager) {
        return new NacosServiceDiscovery(discoveryProperties, nacosServiceManager);
    }
}
