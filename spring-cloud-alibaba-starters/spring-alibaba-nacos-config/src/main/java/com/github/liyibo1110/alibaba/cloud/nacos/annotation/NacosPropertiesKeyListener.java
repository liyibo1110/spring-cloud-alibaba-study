package com.github.liyibo1110.alibaba.cloud.nacos.annotation;

import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.common.utils.CollectionUtils;

import java.util.Set;

/**
 * @author liyibo
 * @date 2026-06-08 13:44
 */
public abstract class NacosPropertiesKeyListener extends AbstractConfigChangeListener {
    Set<String> interestedKeys;

    Set<String> interestedKeyPrefixes;

    NacosPropertiesKeyListener(Object target) {
        super(target);
    }

    NacosPropertiesKeyListener(Object target, Set<String> interestedKeys) {
        this(target);
        this.interestedKeys = interestedKeys;
    }

    public NacosPropertiesKeyListener(Object target, Set<String> interestedKeys, Set<String> interestedKeyPrefixes) {
        this(target);
        this.interestedKeys = interestedKeys;
        this.interestedKeyPrefixes = interestedKeyPrefixes;
    }

    @Override
    public final void receiveConfigChange(ConfigChangeEvent event) {
        if (CollectionUtils.isNotEmpty(interestedKeys) || CollectionUtils.isNotEmpty(interestedKeyPrefixes)) {
            boolean foundInterested = false;
            for (ConfigChangeItem changeItem : event.getChangeItems()) {
                if (interestedKeys != null && interestedKeys.contains(changeItem.getKey())) {
                    foundInterested = true;
                    break;
                }
                if (interestedKeyPrefixes != null) {
                    for (String prefix : interestedKeyPrefixes) {
                        if (changeItem.getKey().startsWith(prefix)) {
                            foundInterested = true;
                            break;
                        }
                    }
                }
            }
            if (!foundInterested)
                return;
        }
        configChanged(event);
    }

    @Override
    public String toString() {
        return "NacosPropertiesKeyListener{" + "interestedKeys=" + interestedKeys + ", interestedKeyPrefixes=" + interestedKeyPrefixes + '}' + "@" + hashCode();
    }

    public abstract void configChanged(ConfigChangeEvent event);
}
