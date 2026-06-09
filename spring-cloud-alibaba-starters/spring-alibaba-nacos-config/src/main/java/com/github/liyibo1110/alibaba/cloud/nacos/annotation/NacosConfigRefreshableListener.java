package com.github.liyibo1110.alibaba.cloud.nacos.annotation;

import com.alibaba.nacos.api.config.listener.AbstractListener;

/**
 * @author liyibo
 * @date 2026-06-08 13:43
 */
public abstract class NacosConfigRefreshableListener extends AbstractListener implements TargetRefreshable {
    Object target;

    NacosConfigRefreshableListener(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    @Override
    public void setTarget(Object target) {
        this.target = target;
    }
}
