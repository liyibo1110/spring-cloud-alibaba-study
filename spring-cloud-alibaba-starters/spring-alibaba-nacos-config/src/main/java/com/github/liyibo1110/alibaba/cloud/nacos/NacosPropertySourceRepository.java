package com.github.liyibo1110.alibaba.cloud.nacos;

import com.github.liyibo1110.alibaba.cloud.nacos.client.NacosPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liyibo
 * @date 2026-05-26 21:51
 */
public final class NacosPropertySourceRepository {

    /** key是dataId + group */
    private final static ConcurrentHashMap<String, NacosPropertySource> NACOS_PROPERTY_SOURCE_REPOSITORY = new ConcurrentHashMap<>();

    private NacosPropertySourceRepository() {}

    public static List<NacosPropertySource> getAll() {
        return new ArrayList<>(NACOS_PROPERTY_SOURCE_REPOSITORY.values());
    }

    @Deprecated
    public static void collectNacosPropertySources(NacosPropertySource nacosPropertySource) {
        NACOS_PROPERTY_SOURCE_REPOSITORY.putIfAbsent(nacosPropertySource.getDataId(), nacosPropertySource);
    }

    @Deprecated
    public static NacosPropertySource getNacosPropertySource(String dataId) {
        return NACOS_PROPERTY_SOURCE_REPOSITORY.get(dataId);
    }

    /**
     * 将给定的NacosPropertySource存入map
     */
    public static void collectNacosPropertySource(NacosPropertySource nacosPropertySource) {
        NACOS_PROPERTY_SOURCE_REPOSITORY.putIfAbsent(getMapKey(nacosPropertySource.getDataId(), nacosPropertySource.getGroup()), nacosPropertySource);
    }

    /**
     * 根据给定的dataId + group，获取对应的NacosPropertySource。
     */
    public static NacosPropertySource getNacosPropertySource(String dataId, String group) {
        return NACOS_PROPERTY_SOURCE_REPOSITORY.get(getMapKey(dataId, group));
    }

    /**
     * 生成key
     */
    public static String getMapKey(String dataId, String group) {
        return String.join(NacosConfigProperties.COMMAS, String.valueOf(dataId), String.valueOf(group));
    }
}
