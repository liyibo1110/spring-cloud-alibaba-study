package com.github.liyibo1110.alibaba.cloud.nacos.refresh;

import org.springframework.context.ApplicationEvent;

/**
 * config的刷新事件，
 * @author liyibo
 * @date 2026-05-26 22:11
 */
public class NacosConfigRefreshEvent extends ApplicationEvent {
    String dataId;
    String group;

    private Object event;
    private String eventDesc;

    public NacosConfigRefreshEvent(Object source, Object event, String eventDesc) {
        super(source);
        this.event = event;
        this.eventDesc = eventDesc;
    }

    public Object getEvent() {
        return this.event;
    }

    public String getEventDesc() {
        return this.eventDesc;
    }

    public String getDataId() {
        return dataId;
    }

    void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getGroup() {
        return group;
    }

    void setGroup(String group) {
        this.group = group;
    }
}
