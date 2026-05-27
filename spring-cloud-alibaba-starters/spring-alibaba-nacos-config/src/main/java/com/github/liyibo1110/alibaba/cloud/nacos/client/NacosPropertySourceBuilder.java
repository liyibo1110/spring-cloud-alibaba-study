package com.github.liyibo1110.alibaba.cloud.nacos.client;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.github.liyibo1110.alibaba.cloud.nacos.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 生成NacosPropertySource对象的工厂。
 * @author liyibo
 * @date 2026-05-25 10:07
 */
public class NacosPropertySourceBuilder {

    private static final Logger log = LoggerFactory.getLogger(NacosPropertySourceBuilder.class);

    private ConfigService configService;

    private long timeout;

    public NacosPropertySourceBuilder(ConfigService configService, long timeout) {
        this.configService = configService;
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * 生成NacosPropertySource对象。
     */
    public NacosPropertySource build(String dataId, String group, String fileExtension, boolean isRefreshable) {
        List<PropertySource<?>> propertySources = loadNacosData(dataId, group, fileExtension);
        NacosPropertySource nacosPropertySource = new NacosPropertySource(propertySources, group, dataId, new Date(), isRefreshable);
        // 加入到repository
        NacosPropertySourceRepository.collectNacosPropertySource(nacosPropertySource);
        return nacosPropertySource;
    }

    private List<PropertySource<?>> loadNacosData(String dataId, String group, String fileExtension) {
        String data = null;
        try {
            // 先尝试从nacos里面取config
            String configSnapshot = NacosSnapshotConfigManager.getAndRemoveConfigSnapshot(dataId, group);
            if (StringUtils.isEmpty(configSnapshot)) {
                log.debug("get config from nacos, dataId: {}, group: {}", dataId, group);
                data = configService.getConfig(dataId, group, timeout);
            } else {
                log.debug("get config from memory snapshot, dataId: {}, group: {}", dataId, group);
                data = configSnapshot;
            }

            if (StringUtils.isEmpty(data)) {
                log.warn("Ignore the empty nacos configuration and get it based on dataId[{}] & group[{}]", dataId, group);
                return Collections.emptyList();
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("Loading nacos data, dataId: '%s', group: '%s', data: %s", dataId, group, data));
            }

            return NacosDataParserHandler.getInstance().parseNacosData(dataId, data, fileExtension);
        } catch (NacosException e) {
            log.error("get data from Nacos error,dataId:{} ", dataId, e);
        } catch (Exception e) {
            log.error("parse data from Nacos error,dataId:{},data:{}", dataId, data, e);
        }

        return Collections.emptyList();
    }
}
