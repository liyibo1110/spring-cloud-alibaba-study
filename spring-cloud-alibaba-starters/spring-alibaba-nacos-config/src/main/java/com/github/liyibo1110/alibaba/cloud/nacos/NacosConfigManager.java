package com.github.liyibo1110.alibaba.cloud.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.github.liyibo1110.alibaba.cloud.nacos.diagnostics.analyzer.NacosConnectionFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 负责：
 * 1、持有NacosConfigProperties
 * 2、创建并缓存，以及对外提供ConfigService
 * 可以理解为：Spring Cloud Alibaba到Nacos Client库的桥接组件。
 * @author liyibo
 * @date 2026-05-19 18:26
 */
public class NacosConfigManager {

    private static final Logger log = LoggerFactory.getLogger(NacosConfigManager.class);

    private static ConfigService service;

    private static NacosConfigManager INSTANCE;

    private NacosConfigProperties nacosConfigProperties;

    public NacosConfigManager(NacosConfigProperties nacosConfigProperties) {
        this.nacosConfigProperties = nacosConfigProperties;
    }

    public static NacosConfigManager getInstance() {
        return INSTANCE;
    }

    public static NacosConfigManager getInstance(NacosConfigProperties properties) {
        if (INSTANCE != null)
            return INSTANCE;
        synchronized (NacosConfigManager.class) {
            if (INSTANCE == null) {
                INSTANCE = new NacosConfigManager(properties);
                INSTANCE.createConfigService(properties);
            }
        }
        return INSTANCE;
    }

    private ConfigService createConfigService(NacosConfigProperties nacosConfigProperties) {
        try {
            if (Objects.isNull(service))
                service = NacosFactory.createConfigService(nacosConfigProperties.assembleConfigServiceProperties());
        } catch (NacosException e) {
            log.error(e.getMessage());
            throw new NacosConnectionFailureException(nacosConfigProperties.getServerAddr(), e.getMessage(), e);
        }
        return service;
    }

    public ConfigService getConfigService() {
        if (Objects.isNull(service))
            createConfigService(this.nacosConfigProperties);

        return service;
    }

    public NacosConfigProperties getNacosConfigProperties() {
        return nacosConfigProperties;
    }
}
