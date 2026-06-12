package com.github.liyibo1110.alibaba.cloud.nacos.client;

import com.alibaba.cloud.nacos.refresh.NacosConfigRefreshEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

/**
 * 负责刷新事件的桥接，把NacosConfigRefreshEvent转换成Spring Cloud RefreshEvent。
 * 附带动态刷新的路线图：
 * NacosContextRefresher
 *     ↓
 * 监听Nacos dataId/group变化
 *     ↓
 * 发布NacosConfigRefreshEvent
 *     ↓
 * NacosConfigRefreshEventListener（本组件）
 *     ↓
 * 发布Spring Cloud RefreshEvent
 *     ↓
 * Spring Cloud Refresh 体系刷新 Environment / RefreshScope等
 *
 * @author liyibo
 * @date 2026-06-11 11:15
 */
public class NacosConfigRefreshEventListener implements SmartApplicationListener, ApplicationContextAware {

    private final static Logger log = LoggerFactory.getLogger(NacosConfigRefreshEventListener.class);

    private ApplicationContext applicationContext;

    /**
     * 只处理NacosConfigRefreshEvent类型的消息。
     */
    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return NacosConfigRefreshEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 直接转换成Spring Cloud RefreshEvent。
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        applicationContext.publishEvent(new RefreshEvent(event.getSource(), null, "Refresh Nacos config"));
        if (log.isDebugEnabled())
            log.debug(String.format("Refresh Nacos config group=%s,dataId=%s", ((NacosConfigRefreshEvent) event).getGroup(), ((NacosConfigRefreshEvent) event).getDataId()));
    }
}
