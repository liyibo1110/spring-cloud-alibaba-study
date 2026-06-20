package com.github.liyibo1110.alibaba.cloud.nacos.registry;

import com.github.liyibo1110.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosServiceManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-19 15:10
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnNacosDiscoveryEnabled
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@AutoConfigureAfter({ AutoServiceRegistrationConfiguration.class,
        AutoServiceRegistrationAutoConfiguration.class,
        NacosDiscoveryAutoConfiguration.class })
public class NacosServiceRegistryAutoConfiguration {

    @Bean
    public NacosServiceRegistry nacosServiceRegistry(NacosServiceManager nacosServiceManager,
                                                     NacosDiscoveryProperties nacosDiscoveryProperties) {
        return new NacosServiceRegistry(nacosServiceManager, nacosDiscoveryProperties);
    }

    @Bean
    @ConditionalOnBean(AutoServiceRegistrationProperties.class)
    public NacosRegistration nacosRegistration(ObjectProvider<List<NacosRegistrationCustomizer>> registrationCustomizers,
                                               NacosDiscoveryProperties nacosDiscoveryProperties,
                                               ApplicationContext context) {
        return new NacosRegistration(registrationCustomizers.getIfAvailable(), nacosDiscoveryProperties, context);
    }

    @Bean
    @ConditionalOnBean(AutoServiceRegistrationProperties.class)
    public NacosAutoServiceRegistration nacosAutoServiceRegistration(
            NacosServiceRegistry registry,
            AutoServiceRegistrationProperties autoServiceRegistrationProperties,
            NacosRegistration registration) {
        return new NacosAutoServiceRegistration(registry, autoServiceRegistrationProperties, registration);
    }

    @Bean
    @ConditionalOnBean(NacosAutoServiceRegistration.class)
    public NacosGracefulShutdownDelegate nacosGracefulShutdownDelegate(
            NacosAutoServiceRegistration nacosAutoServiceRegistration,
            NacosDiscoveryProperties nacosDiscoveryProperties) {
        return new NacosGracefulShutdownDelegate(nacosAutoServiceRegistration, nacosDiscoveryProperties);
    }
}
