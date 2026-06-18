package com.github.liyibo1110.alibaba.cloud.nacos.event;

import com.github.liyibo1110.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.springframework.context.ApplicationEvent;

/**
 * @author liyibo
 * @date 2026-06-17 13:55
 */
public class NacosDiscoveryInfoChangedEvent extends ApplicationEvent {

    public NacosDiscoveryInfoChangedEvent(NacosDiscoveryProperties nacosDiscoveryProperties) {
        super(nacosDiscoveryProperties);
    }

    @Override
    public NacosDiscoveryProperties getSource() {
        return (NacosDiscoveryProperties) super.getSource();
    }
}
