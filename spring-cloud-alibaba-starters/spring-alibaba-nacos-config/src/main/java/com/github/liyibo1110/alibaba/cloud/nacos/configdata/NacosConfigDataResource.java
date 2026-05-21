package com.github.liyibo1110.alibaba.cloud.nacos.configdata;

import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigProperties;
import org.apache.commons.logging.Log;
import org.springframework.boot.context.config.ConfigDataResource;
import org.springframework.boot.context.config.Profiles;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Nacos远程配置的一张任务单，保存着要被加载的信息，最终由NacosConfigDataLoader来加载自己。
 * 自身
 * @author liyibo
 * @date 2026-05-21 11:46
 */
public class NacosConfigDataResource extends ConfigDataResource {

    /** nacos config的全局配置 */
    private final NacosConfigProperties properties;

    /** 语义会传递给Loader，代表远程配置不存在、加载失败、无法解析时，是否还允许应用继续启动？ */
    private final boolean optional;

    /** 即local,dev,prod这样的环境值 */
    private final Profiles profiles;

    private final Log log;

    private final NacosItemConfig config;

    public NacosConfigDataResource(NacosConfigProperties properties, boolean optional, Profiles profiles, Log log, NacosItemConfig config) {
        this.properties = properties;
        this.optional = optional;
        this.profiles = profiles;
        this.log = log;
        this.config = config;
    }

    public NacosConfigProperties getProperties() {
        return this.properties;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public String getProfiles() {
        return StringUtils.collectionToCommaDelimitedString(getAcceptedProfiles());
    }

    List<String> getAcceptedProfiles() {
        return this.profiles.getAccepted();
    }

    public Log getLog() {
        return this.log;
    }

    public NacosItemConfig getConfig() {
        return config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        NacosConfigDataResource that = (NacosConfigDataResource) o;
        return optional == that.optional && Objects.equals(properties, that.properties)
                && Objects.equals(profiles, that.profiles)
                && Objects.equals(log, that.log) && Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties, optional, profiles, log, config);
    }

    @Override
    public String toString() {
        return "NacosConfigDataResource{" + "properties=" + properties + ", optional="
                + optional + ", profiles=" + profiles + ", config=" + config + '}';
    }

    /**
     * 具体要加载的dataId / group / suffix，即要从Nacos加载的一个具体配置。
     */
    public static class NacosItemConfig {
        /** 默认值是DEFAULT_GROUP */
        private String group;

        /** 例如application.yml / user-service.yml / user-service-dev.yml / common.yml */
        private String dataId;

        /** 配置文件后缀，properties / yml */
        private String suffix;

        /** 是否支持动态刷新，为true则会将这个配置项，注册到listener */
        private boolean refreshEnabled;

        /** 这个配置项的来源或偏好标记，用于后续区分不同类型的Nacos配置项，类似tag */
        private String preference;

        public NacosItemConfig() {}

        public NacosItemConfig(String group, String dataId, String suffix, boolean refreshEnabled, String preference) {
            this.group = group;
            this.dataId = dataId;
            this.suffix = suffix;
            this.refreshEnabled = refreshEnabled;
            this.preference = preference;
        }

        public NacosItemConfig setGroup(String group) {
            this.group = group;
            return this;
        }

        public NacosItemConfig setDataId(String dataId) {
            this.dataId = dataId;
            return this;
        }

        public NacosItemConfig setSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public NacosItemConfig setRefreshEnabled(boolean refreshEnabled) {
            this.refreshEnabled = refreshEnabled;
            return this;
        }

        public NacosItemConfig setPreference(String preference) {
            this.preference = preference;
            return this;
        }

        public String getGroup() {
            return group;
        }

        public String getDataId() {
            return dataId;
        }

        public String getSuffix() {
            return suffix;
        }

        public boolean isRefreshEnabled() {
            return refreshEnabled;
        }

        public String getPreference() {
            return preference;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;

            if (o == null || getClass() != o.getClass())
                return false;

            NacosItemConfig that = (NacosItemConfig) o;
            return refreshEnabled == that.refreshEnabled
                    && Objects.equals(group, that.group)
                    && Objects.equals(dataId, that.dataId)
                    && Objects.equals(suffix, that.suffix)
                    && Objects.equals(preference, that.preference);
        }

        @Override
        public int hashCode() {
            return Objects.hash(group, dataId, suffix, refreshEnabled, preference);
        }

        @Override
        public String toString() {
            return "NacosItemConfig{" + "group='" + group + '\'' + ", dataId='" + dataId
                    + '\'' + ", suffix='" + suffix + '\'' + ", refreshEnabled="
                    + refreshEnabled + ", preference=" + preference + '}';
        }
    }
}
