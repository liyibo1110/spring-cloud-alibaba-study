package com.github.liyibo1110.alibaba.cloud.nacos.discovery;

import com.github.liyibo1110.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-06-22 12:38
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnBlockingDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
@AutoConfigureAfter(value = NacosDiscoveryAutoConfiguration.class,
        name = "de.codecentric.boot.admin.server.cloud.config.AdminServerDiscoveryAutoConfiguration")
public class NacosDiscoveryHeartBeatConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Conditional(NacosDiscoveryHeartBeatCondition.class)
    public NacosDiscoveryHeartBeatPublisher nacosDiscoveryHeartBeatPublisher(NacosDiscoveryProperties nacosDiscoveryProperties) {
        return new NacosDiscoveryHeartBeatPublisher(nacosDiscoveryProperties);
    }

    private static class NacosDiscoveryHeartBeatCondition extends AnyNestedCondition {

        NacosDiscoveryHeartBeatCondition()  {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        /**
         * Spring Cloud Gateway HeartBeat
         */
        @ConditionalOnProperty(value = "spring.cloud.gateway.server.webflux.discovery.locator.enabled", matchIfMissing = false)
        static class GatewayLocatorHeartBeatEnabled {}

        /**
         * Spring Boot Admin HeartBeat
         */
        @ConditionalOnBean(type = "de.codecentric.boot.admin.server.cloud.discovery.InstanceDiscoveryListener")
        static class SpringBootAdminHeartBeatEnabled {}

        /**
         * Nacos HeartBeat
         */
        @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.heart-beat.enabled", matchIfMissing = false)
        static class NacosDiscoveryHeartBeatEnabled {}
    }
}
