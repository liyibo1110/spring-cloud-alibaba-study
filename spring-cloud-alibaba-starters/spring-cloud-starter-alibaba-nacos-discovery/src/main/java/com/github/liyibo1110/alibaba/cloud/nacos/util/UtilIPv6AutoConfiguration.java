package com.github.liyibo1110.alibaba.cloud.nacos.util;

import com.github.liyibo1110.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-06-14 13:24
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
public class UtilIPv6AutoConfiguration {

    public UtilIPv6AutoConfiguration() {}

    @Bean
    @ConditionalOnMissingBean
    public InetIPv6Utils inetIPv6Utils(InetUtilsProperties properties) {
        return new InetIPv6Utils(properties);
    }
}
