package com.github.liyibo1110.alibaba.cloud.nacos;

import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 只负责生成NacosServiceManager对象。
 * @author liyibo
 * @date 2026-06-18 11:06
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
public class NacosServiceAutoConfiguration {

    @Bean
    public NacosServiceManager nacosServiceManager() {
        return new NacosServiceManager();
    }
}
