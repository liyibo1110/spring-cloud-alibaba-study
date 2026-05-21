package com.github.liyibo1110.alibaba.cloud.nacos.configdata;

import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author liyibo
 * @date 2026-05-21 12:03
 */
public class NacosConfigDataLoadProperties extends NacosConfigProperties {
    private Map<String, String> config = new HashMap<>();

    /**
     * 用config填充给定的Properties。
     */
    @Override
    protected void enrichNacosConfigProperties(Properties nacosConfigProperties) {
        config.forEach((k, v) -> nacosConfigProperties.putIfAbsent(resolveKey(k), String.valueOf(v)));
    }

    Map<String, String> getConfig() {
        return config;
    }

    void setConfig(Map<String, String> config) {
        this.config = config;
    }
}
