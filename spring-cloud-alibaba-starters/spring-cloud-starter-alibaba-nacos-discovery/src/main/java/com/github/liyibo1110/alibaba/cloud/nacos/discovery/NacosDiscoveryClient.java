package com.github.liyibo1110.alibaba.cloud.nacos.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Spring Cloud会调用这个服务发现组件，询问：
 * 1、有哪些服务？
 * 2、某个serviceId有哪些实例？
 * 这个核心组件负责调用内部的NamingService来实际查询（实际是通过NacosServiceDiscovery封装组件）。
 * @author liyibo
 * @date 2026-06-21 13:37
 */
public class NacosDiscoveryClient implements DiscoveryClient {

    private static final Logger log = LoggerFactory.getLogger(NacosDiscoveryClient.class);

    public static final String DESCRIPTION = "Spring Cloud Nacos Discovery Client";

    private NacosServiceDiscovery serviceDiscovery;

    @Value("${spring.cloud.nacos.discovery.failure-tolerance-enabled:false}")
    private boolean failureToleranceEnabled;

    public NacosDiscoveryClient(NacosServiceDiscovery nacosServiceDiscovery) {
        this.serviceDiscovery = nacosServiceDiscovery;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        try {
            return Optional.of(serviceDiscovery.getInstances(serviceId))
                    .map(instances -> {
                        ServiceCache.setInstances(serviceId, instances);
                        return instances;
                    }).get();
        } catch (Exception e) {
            if (failureToleranceEnabled) {
                return ServiceCache.getInstances(serviceId);
            }
            throw new RuntimeException(
                    "Can not get hosts from nacos server. serviceId: " + serviceId, e);
        }
    }

    @Override
    public List<String> getServices() {
        try {
            return Optional.of(serviceDiscovery.getServices()).map(services -> {
                ServiceCache.setServiceIds(services);
                return services;
            }).get();
        } catch (Exception e) {
            log.error("get service name from nacos server failed.", e);
            return failureToleranceEnabled
                    ? ServiceCache.getServiceIds()
                    : Collections.emptyList();
        }
    }
}
