package com.github.liyibo1110.alibaba.cloud.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
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
