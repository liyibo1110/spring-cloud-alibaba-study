package com.github.liyibo1110.alibaba.cloud.nacos.registry;

import com.alibaba.nacos.common.utils.ThreadUtils;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;

/**
 * 监听ContextClosedEvent事件，触发关闭NacosAutoServiceRegistration。
 * @author liyibo
 * @date 2026-06-19 14:58
 */
public class NacosGracefulShutdownDelegate implements ApplicationListener<ContextClosedEvent>, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(NacosGracefulShutdownDelegate.class);

    private final NacosAutoServiceRegistration autoServiceRegistration;

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    private ApplicationContext applicationContext;

    public NacosGracefulShutdownDelegate(NacosAutoServiceRegistration autoServiceRegistration,
                                         NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.autoServiceRegistration = autoServiceRegistration;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextClosedEvent event) {
        // should NOT be executed if ContextClosedEvent published by sub contexts
        if (!applicationContext.equals(event.getApplicationContext())) {
            log.debug("Nacos client graceful shutdown will NOT be executed for Spring context source: {}", event.getApplicationContext());
            return;
        }
        doGracefulShutdown();
    }

    protected void doGracefulShutdown() {
        try {
            autoServiceRegistration.stop();
            Integer gracefulShutdownWaitTime = this.nacosDiscoveryProperties.getGracefulShutdownWaitTime();
            if (gracefulShutdownWaitTime != null && gracefulShutdownWaitTime > 0)
                ThreadUtils.sleep(gracefulShutdownWaitTime);
            log.info("Nacos client graceful shutdown has been executed successfully. Graceful shutdown wait time is {}", gracefulShutdownWaitTime);
        } catch (Throwable t) {
            log.error("Error occurred while performing Nacos client graceful shutdown", t);
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        // need wait for graceful shutdown
        return false;
    }
}
