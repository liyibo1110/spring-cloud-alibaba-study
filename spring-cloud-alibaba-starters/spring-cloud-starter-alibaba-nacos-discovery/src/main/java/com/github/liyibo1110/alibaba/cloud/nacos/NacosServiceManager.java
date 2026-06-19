package com.github.liyibo1110.alibaba.cloud.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Properties;

import static com.alibaba.nacos.api.NacosFactory.createMaintainService;
import static com.alibaba.nacos.api.NacosFactory.createNamingService;

/**
 * 封装了nacos自有的NamingService和NamingMaintainService组件，可以类比config那边的NacosConfigManager组件。
 * @author liyibo
 * @date 2026-06-18 10:59
 */
public class NacosServiceManager {

    private static final Logger log = LoggerFactory.getLogger(NacosServiceManager.class);

    private NacosDiscoveryProperties nacosDiscoveryProperties;

    private volatile NamingService namingService;

    private volatile NamingMaintainService namingMaintainService;

    /**
     * 延迟加载。
     */
    public NamingService getNamingService() {
        if (Objects.isNull(this.namingService))
            buildNamingService(nacosDiscoveryProperties.getNacosProperties());
        return namingService;
    }

    @Deprecated
    public NamingService getNamingService(Properties properties) {
        if (Objects.isNull(this.namingService))
            buildNamingService(properties);
        return namingService;
    }

    /**
     * 延迟加载。
     */
    public NamingMaintainService getNamingMaintainService(Properties properties) {
        if (Objects.isNull(namingMaintainService))
            buildNamingMaintainService(properties);
        return namingMaintainService;
    }

    /**
     * 判断传入的properties，是否和原来的properties相比发生了变化。
     */
    public boolean isNacosDiscoveryInfoChanged(NacosDiscoveryProperties currentNacosDiscoveryPropertiesCache) {
        if (Objects.isNull(this.nacosDiscoveryProperties) || this.nacosDiscoveryProperties.equals(currentNacosDiscoveryPropertiesCache))
            return false;
        return true;
    }

    private NamingMaintainService buildNamingMaintainService(Properties properties) {
        if (Objects.isNull(namingMaintainService)) {
            synchronized (NacosServiceManager.class) {
                if (Objects.isNull(namingMaintainService))
                    namingMaintainService = createNamingMaintainService(properties);
            }
        }
        return namingMaintainService;
    }

    private NamingService buildNamingService(Properties properties) {
        if (Objects.isNull(namingService)) {
            synchronized (NacosServiceManager.class) {
                if (Objects.isNull(namingService))
                    namingService = createNewNamingService(properties);
            }
        }
        return namingService;
    }

    /**
     * 委托nacos的NacosFactory来构造真正的NamingService对象。
     */
    private NamingService createNewNamingService(Properties properties) {
        try {
            return createNamingService(properties);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 委托nacos的NamingMaintainFactory来构造真正的NamingMaintainService对象。
     */
    private NamingMaintainService createNamingMaintainService(Properties properties) {
        try {
            return createMaintainService(properties);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    public void nacosServiceShutDown() throws NacosException {
        if (Objects.nonNull(this.namingService)) {
            this.namingService.shutDown();
            this.namingService = null;
        }
        if (Objects.nonNull(this.namingMaintainService)) {
            this.namingMaintainService.shutDown();
            this.namingMaintainService = null;
        }
    }

    public void setNacosDiscoveryProperties(NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }
}
