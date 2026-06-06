package com.github.liyibo1110.alibaba.cloud.nacos.annotation;

import com.alibaba.nacos.api.config.listener.Listener;

/**
 * @author liyibo
 * @date 2026-06-05 13:24
 */
interface TargetRefreshable extends Listener {

    Object getTarget();

    void setTarget(Object target);
}
