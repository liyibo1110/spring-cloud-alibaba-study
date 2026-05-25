package com.github.liyibo1110.alibaba.cloud.nacos.client;

import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigProperties;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring的PropertySource扩展。
 * @author liyibo
 * @date 2026-05-23 15:53
 */
public class NacosPropertySource extends MapPropertySource {

    private final String group;

    private final String dataId;

    private final Date timestamp;

    /** 是否支持动态更新 */
    private final boolean isRefreshable;

    NacosPropertySource(String group, String dataId, Map<String, Object> source, Date timestamp, boolean isRefreshable) {
        super(String.join(NacosConfigProperties.COMMAS, dataId, group), source);
        this.group = group;
        this.dataId = dataId;
        this.timestamp = timestamp;
        this.isRefreshable = isRefreshable;
    }

    public NacosPropertySource(List<PropertySource<?>> propertySources, String group, String dataId, Date timestamp, boolean isRefreshable) {
        this(group, dataId, getSourceMap(group, dataId, propertySources), timestamp, isRefreshable);
    }

    private static Map<String, Object> getSourceMap(String group, String dataId, List<PropertySource<?>> propertySources) {
        if (CollectionUtils.isEmpty(propertySources))
            return Collections.emptyMap();

        // If only one, return the internal element, otherwise wrap it.
        if (propertySources.size() == 1) {
            PropertySource propertySource = propertySources.get(0);
            if (propertySource != null && propertySource.getSource() instanceof Map source)
                return source;
        }

        Map<String, Object> sourceMap = new LinkedHashMap<>();
        List<PropertySource<?>> otherTypePropertySources = new ArrayList<>();
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource == null) {
                continue;
            }
            if (propertySource instanceof MapPropertySource mapPropertySource) {
                // If the Nacos configuration file uses "---" to separate property name,
                // propertySources will be multiple documents, and every document is a
                // map.
                // see org.springframework.boot.env.YamlPropertySourceLoader#load
                Map<String, Object> source = mapPropertySource.getSource();
                sourceMap.putAll(source);
            } else {
                otherTypePropertySources.add(propertySource);
            }
        }

        // Other property sources which is not instanceof MapPropertySource will be put as
        // it is,
        // and the internal elements cannot be directly retrieved,
        // so the user needs to implement the retrieval logic by himself
        if (!otherTypePropertySources.isEmpty())
            sourceMap.put(String.join(NacosConfigProperties.COMMAS, dataId, group), otherTypePropertySources);

        return sourceMap;
    }

    public String getGroup() {
        return this.group;
    }

    public String getDataId() {
        return dataId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isRefreshable() {
        return isRefreshable;
    }
}
