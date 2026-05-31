package com.github.liyibo1110.alibaba.cloud.nacos.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liyibo
 * @date 2026-05-30 14:17
 */
public final class NacosSnapshotConfigManager {
    private static final Logger log = LoggerFactory.getLogger(NacosSnapshotConfigManager.class);

    private NacosSnapshotConfigManager() {}

    private static final Map<String, String> CONFIG_INFO_SNAPSHOT_MAP = new ConcurrentHashMap<>(8);

    private static final int MAX_SNAPSHOT_COUNT = 100;

    private static String formatConfigSnapshotKey(String dataId, String group) {
        return dataId + "@" + group;
    }

    /**
     * 从缓存中获取特定dataId / group的配置值，并且移除。
     */
    public static String getAndRemoveConfigSnapshot(String dataId, String group) {
        String configInfo = CONFIG_INFO_SNAPSHOT_MAP.get(formatConfigSnapshotKey(dataId, group));
        removeConfigSnapshot(dataId, group);
        return configInfo;
    }

    public static void putConfigSnapshot(String dataId, String group, String configInfo) {
        try {
            if (CONFIG_INFO_SNAPSHOT_MAP.size() > MAX_SNAPSHOT_COUNT) {
                Iterator<Map.Entry<String, String>> iterator = CONFIG_INFO_SNAPSHOT_MAP.entrySet().iterator();
                iterator.next();
                iterator.remove();
            }
            String snapshotKey = formatConfigSnapshotKey(dataId, group);
            if (configInfo == null)
                CONFIG_INFO_SNAPSHOT_MAP.remove(snapshotKey);
            else
                CONFIG_INFO_SNAPSHOT_MAP.put(snapshotKey, configInfo);
        } catch (Exception e) {
            log.warn("remove nacos config snapshot error", e);
        }
    }

    /**
     * 从缓存中移除特定dataId / group的配置值。
     */
    public static void removeConfigSnapshot(String dataId, String group) {
        CONFIG_INFO_SNAPSHOT_MAP.remove(formatConfigSnapshotKey(dataId, group));
    }
}
