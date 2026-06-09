package com.github.liyibo1110.alibaba.cloud.nacos.annotation;

import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.alibaba.nacos.client.config.impl.ConfigChangeHandler;

import java.util.Map;

/**
 * 作为Nacos Listener的父类，收到Nacos configInfo后，提供模板方法。
 * @author liyibo
 * @date 2026-06-08 12:50
 */
public abstract class AbstractConfigChangeListener extends AbstractSharedListener implements TargetRefreshable {

    String lastContent;

    Object target;

    public AbstractConfigChangeListener(Object target) {
        this.target = target;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public void setTarget(Object target) {
        this.target = target;
    }

    protected void setLastContent(String lastContent) {
        this.lastContent = lastContent;
    }

    @Override
    public void innerReceive(String dataId, String group, String configInfo) {
        Map<String, ConfigChangeItem> data = null;
        try {
            data = ConfigChangeHandler.getInstance().parseChangeData(lastContent, configInfo, type(dataId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ConfigChangeEvent event = new ConfigChangeEvent(data);
        receiveConfigChange(event);
        lastContent = configInfo;
    }

    private String type(String dataId) {
        if (dataId.endsWith(".yml") || dataId.endsWith(".yaml"))
            return "yaml";

        return "properties";
    }

    abstract void receiveConfigChange(ConfigChangeEvent event);
}
