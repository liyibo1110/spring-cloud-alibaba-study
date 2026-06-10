package com.github.liyibo1110.alibaba.cloud.nacos;

import com.alibaba.cloud.nacos.NacosPropertiesPrefixProvider;

/**
 * 提供nacos properties prefix。
 * @author liyibo
 * @date 2026-06-09 11:18
 */
public class SpringCloudNacosPropertiesPrefixProvider implements NacosPropertiesPrefixProvider {
    @Override
    public String getPrefix() {
        return "spring.cloud.nacos";
    }
}
