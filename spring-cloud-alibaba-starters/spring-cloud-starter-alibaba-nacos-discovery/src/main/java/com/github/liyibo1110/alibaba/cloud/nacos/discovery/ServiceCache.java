package com.github.liyibo1110.alibaba.cloud.nacos.discovery;

import org.springframework.cloud.client.ServiceInstance;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiceInstance的cache工具组件。
 * @author liyibo
 * @date 2026-06-21 12:56
 */
public final class ServiceCache {

    private ServiceCache() {}

    /** service ids */
    private static List<String> services = Collections.emptyList();

    /** cache */
    private static Map<String, List<ServiceInstance>> instancesMap = new ConcurrentHashMap<>();

    /** put */
    public static void setInstances(String serviceId, List<ServiceInstance> instances) {
        instancesMap.put(serviceId, Collections.unmodifiableList(instances));
    }

    /** get */
    public static List<ServiceInstance> getInstances(String serviceId) {
        return Optional.ofNullable(instancesMap.get(serviceId)).orElse(Collections.emptyList());
    }

    @Deprecated
    public static void set(List<String> serviceIds) {
        services = Collections.unmodifiableList(serviceIds);
    }

    public static void setServiceIds(List<String> serviceIds) {
        services = Collections.unmodifiableList(serviceIds);
    }

    @Deprecated
    public static List<String> get() {
        return services;
    }

    public static List<String> getServiceIds() {
        return services;
    }
}
