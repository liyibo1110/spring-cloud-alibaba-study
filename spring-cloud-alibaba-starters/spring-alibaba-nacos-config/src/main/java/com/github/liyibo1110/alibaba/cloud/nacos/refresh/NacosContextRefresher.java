package com.github.liyibo1110.alibaba.cloud.nacos.refresh;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigManager;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author liyibo
 * @date 2026-05-27 22:29
 */
public class NacosContextRefresher implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

    private final static Logger log = LoggerFactory.getLogger(NacosContextRefresher.class);

    private static final AtomicLong REFRESH_COUNT = new AtomicLong(0);

    private final boolean isRefreshEnabled;

    private final NacosRefreshHistory nacosRefreshHistory;

    private NacosConfigProperties nacosConfigProperties;

    private ConfigService configService;

    private NacosConfigManager configManager;

    private ApplicationContext applicationContext;

    private AtomicBoolean ready = new AtomicBoolean(false);

    private Map<String, Listener> listenerMap = new ConcurrentHashMap<>(16);

    public NacosContextRefresher(NacosConfigManager nacosConfigManager, NacosRefreshHistory refreshHistory) {
        this.configManager = nacosConfigManager;
        this.nacosConfigProperties = nacosConfigManager.getNacosConfigProperties();
        this.nacosRefreshHistory = refreshHistory;
        this.isRefreshEnabled = this.nacosConfigProperties.isRefreshEnabled();
    }

    public static long getRefreshCount() {
        return REFRESH_COUNT.get();
    }
}
