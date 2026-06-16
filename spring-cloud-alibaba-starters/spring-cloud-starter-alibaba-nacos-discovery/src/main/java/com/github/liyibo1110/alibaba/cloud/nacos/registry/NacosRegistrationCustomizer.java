package com.github.liyibo1110.alibaba.cloud.nacos.registry;

/**
 * NacosRegistration的自定义扩展点。
 * @author liyibo
 * @date 2026-06-16 10:28
 */
public interface NacosRegistrationCustomizer {

    /**
     * 自定义扩展。
     */
    void customize(NacosRegistration registration);
}
