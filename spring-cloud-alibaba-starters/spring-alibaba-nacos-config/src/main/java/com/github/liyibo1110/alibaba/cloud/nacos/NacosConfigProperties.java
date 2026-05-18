package com.github.liyibo1110.alibaba.cloud.nacos;

import com.github.liyibo1110.alibaba.cloud.nacos.utils.PropertySourcesUtils;
import com.github.liyibo1110.alibaba.cloud.nacos.utils.StringUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * nacos相关spring boot属性
 * @author liyibo
 * @date 2026-05-18 10:47
 */
public class NacosConfigProperties {

    public static final String COMMAS = ",";

    public static final String SEPARATOR = "[,]";

    public static final String DEFAULT_NAMESPACE = "public";

    public static final String DEFAULT_ADDRESS = "127.0.0.1:8848";

    private static final Pattern PATTERN = Pattern.compile("-(\\w)");

    private static final Logger log = LoggerFactory.getLogger(NacosConfigProperties.class);

    @Autowired
    @JsonIgnore
    private Environment environment;

    private String serverAddr;

    private String username;

    private String password;

    /** config内容的编码 */
    private String encode;

    private String group = "DEFAULT_GROUP";

    /** dataId prefix */
    private String prefix;

    private String fileExtension = "properties";

    private int timeout = 3000;

    private String maxRetry;

    private String configLongPollTimeout;

    private String configRetryTime;

    /**
     * 如果希望在程序首次加载配置时自行获取配置，并将已注册的监听器用于后续的配置更新，
     * 可以保持原始代码不变，只需添加系统参数：enableRemoteSyncConfig = “true”（但会产生网络开销）；
     * 因此，我们建议您直接使用ConfigService.getConfigAndSignListener
     */
    private boolean enableRemoteSyncConfig = false;

    /** Nacos的端点，即服务的域名，通过该域名可动态获取服务器地址 */
    private String endpoint;

    private String namespace;

    private String accessKey;

    private String secretKey;

    private String ramRoleName;

    /** config server的context path */
    private String contextPath;

    private String clusterName;

    /** dataId name */
    private String name;

    /** spring.cloud.nacos.config.shared-configs[0]=xxx */
    private List<Config> sharedConfigs;

    /** spring.cloud.nacos.config.extension-configs[0]=xxx */
    private List<Config> extensionConfigs;

    private boolean refreshEnabled = true;

    @PostConstruct
    public void init() {
        this.overrideFromEnv();
    }

    private void overrideFromEnv() {
        if (environment == null)
            return;

        String prefix = NacosPropertiesPrefixer.getPrefix(environment);

        if (StringUtils.isEmpty(this.getServerAddr())) {
            String serverAddr = environment.resolvePlaceholders("${" + prefix + ".config.server-addr:}");
            if (StringUtils.isEmpty(serverAddr))
                serverAddr = environment.resolvePlaceholders("${" + prefix + ".server-addr:127.0.0.1:8848}");

            this.setServerAddr(serverAddr);
        }
        if (StringUtils.isEmpty(this.getUsername()))
            this.setUsername(environment.resolvePlaceholders("${" + prefix + ".username:}"));

        if (StringUtils.isEmpty(this.getPassword()))
            this.setPassword(environment.resolvePlaceholders("${" + prefix + ".password:}"));

    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(String maxRetry) {
        this.maxRetry = maxRetry;
    }

    public String getConfigLongPollTimeout() {
        return configLongPollTimeout;
    }

    public void setConfigLongPollTimeout(String configLongPollTimeout) {
        this.configLongPollTimeout = configLongPollTimeout;
    }

    public String getConfigRetryTime() {
        return configRetryTime;
    }

    public void setConfigRetryTime(String configRetryTime) {
        this.configRetryTime = configRetryTime;
    }

    public Boolean getEnableRemoteSyncConfig() {
        return enableRemoteSyncConfig;
    }

    public void setEnableRemoteSyncConfig(Boolean enableRemoteSyncConfig) {
        this.enableRemoteSyncConfig = enableRemoteSyncConfig;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRamRoleName() {
        return ramRoleName;
    }

    public void setRamRoleName(String ramRoleName) {
        this.ramRoleName = ramRoleName;
    }

    public String getEncode() {
        return encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public List<Config> getSharedConfigs() {
        return sharedConfigs;
    }

    public void setSharedConfigs(List<Config> sharedConfigs) {
        this.sharedConfigs = sharedConfigs;
    }

    public List<Config> getExtensionConfigs() {
        return extensionConfigs;
    }

    public void setExtensionConfigs(List<Config> extensionConfigs) {
        this.extensionConfigs = extensionConfigs;
    }

    public boolean isRefreshEnabled() {
        return refreshEnabled;
    }

    public void setRefreshEnabled(boolean refreshEnabled) {
        this.refreshEnabled = refreshEnabled;
    }

    @Deprecated
    @DeprecatedConfigurationProperty(reason = "use spring.config.import instead")
    public String getSharedDataids() {
        return null == getSharedConfigs()
                ? null
                : getSharedConfigs().stream().map(Config::getDataId).collect(Collectors.joining(COMMAS));
    }

    @Deprecated
    public void setSharedDataids(String sharedDataids) {
        if (null != sharedDataids && sharedDataids.trim().length() > 0) {
            List<Config> list = new ArrayList<>();
            Stream.of(sharedDataids.split(SEPARATOR)).forEach(dataId -> list.add(new Config(dataId.trim())));
            this.compatibleSharedConfigs(list);
        }
    }

    @Deprecated
    public String getRefreshableDataids() {
        return null == getSharedConfigs()
                ? null
                : getSharedConfigs().stream().filter(Config::isRefresh).map(Config::getDataId).collect(Collectors.joining(COMMAS));
    }

    @Deprecated
    public void setRefreshableDataids(String refreshableDataids) {
        if (null != refreshableDataids && refreshableDataids.trim().length() > 0) {
            List<Config> list = new ArrayList<>();
            Stream.of(refreshableDataids.split(SEPARATOR)).forEach(dataId -> list.add(new Config(dataId.trim()).setRefresh(true)));
            this.compatibleSharedConfigs(list);
        }
    }

    private void compatibleSharedConfigs(List<Config> configList) {
        if (null != this.getSharedConfigs())
            configList.addAll(this.getSharedConfigs());

        List<Config> result = new ArrayList<>();
        configList.stream().collect(Collectors.groupingBy(cfg -> (cfg.getGroup() + cfg.getDataId()), LinkedHashMap::new, Collectors.toList()))
                .forEach((key, list) -> {
                    list.stream()
                            .reduce((a, b) -> new Config(a.getDataId(), a.getGroup(),
                                    a.isRefresh() || (b != null && b.isRefresh())))
                            .ifPresent(result::add);
                });
        this.setSharedConfigs(result);
    }

    @Deprecated
    @DeprecatedConfigurationProperty(reason = "use spring.config.import instead")
    public List<Config> getExtConfig() {
        return this.getExtensionConfigs();
    }

    @Deprecated
    public void setExtConfig(List<Config> extConfig) {
        this.setExtensionConfigs(extConfig);
    }

    @Deprecated
    public ConfigService configServiceInstance() {
        // The following code will be migrated
        return NacosConfigManager.getInstance(this).getConfigService();
    }

    @Deprecated
    public Properties getConfigServiceProperties() {
        return this.assembleConfigServiceProperties();
    }

    public Properties assembleConfigServiceProperties() {
        Properties properties = new Properties();
        properties.put(SERVER_ADDR, Objects.toString(this.serverAddr, ""));
        properties.put(USERNAME, Objects.toString(this.username, ""));
        properties.put(PASSWORD, Objects.toString(this.password, ""));
        properties.put(ENCODE, Objects.toString(this.encode, ""));
        properties.put(NAMESPACE, this.resolveNamespace());
        properties.put(ACCESS_KEY, Objects.toString(this.accessKey, ""));
        properties.put(SECRET_KEY, Objects.toString(this.secretKey, ""));
        properties.put(RAM_ROLE_NAME, Objects.toString(this.ramRoleName, ""));
        properties.put(CLUSTER_NAME, Objects.toString(this.clusterName, ""));
        properties.put(MAX_RETRY, Objects.toString(this.maxRetry, ""));
        properties.put(CONFIG_LONG_POLL_TIMEOUT, Objects.toString(this.configLongPollTimeout, ""));
        properties.put(CONFIG_RETRY_TIME, Objects.toString(this.configRetryTime, ""));
        properties.put(ENABLE_REMOTE_SYNC_CONFIG, Objects.toString(this.enableRemoteSyncConfig, ""));
        String endpoint = Objects.toString(this.endpoint, "");
        if (endpoint.contains(":")) {
            int index = endpoint.indexOf(":");
            properties.put(ENDPOINT, endpoint.substring(0, index));
            properties.put(ENDPOINT_PORT, endpoint.substring(index + 1));
        } else {
            properties.put(ENDPOINT, endpoint);
        }

        enrichNacosConfigProperties(properties);

        // set default value when serverAddr and endpoint is empty
        if (StringUtils.isEmpty(this.serverAddr) && StringUtils.isEmpty(this.endpoint))
            properties.put(SERVER_ADDR, DEFAULT_ADDRESS);

        return properties;
    }

    private String resolveNamespace() {
        if (DEFAULT_NAMESPACE.equals(this.namespace)) {
            log.info("set nacos config namespace 'public' to ''");
            return "";
        } else {
            return Objects.toString(this.namespace, "");
        }
    }

    protected void enrichNacosConfigProperties(Properties nacosConfigProperties) {
        if (environment == null)
            return;

        String prefix = NacosPropertiesPrefixer.getPrefix(environment);

        Map<String, Object> properties = PropertySourcesUtils
                .getSubProperties((ConfigurableEnvironment) environment, prefix + ".config");
        properties.forEach((k, v) -> nacosConfigProperties.putIfAbsent(resolveKey(k), String.valueOf(v)));
    }

    protected String resolveKey(String key) {
        Matcher matcher = PATTERN.matcher(key);
        StringBuffer sb = new StringBuffer();
        while (matcher.find())
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase(Locale.ROOT));

        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "NacosConfigProperties{" + "serverAddr='" + serverAddr + '\''
                + ", encode='" + encode + '\'' + ", group='" + group + '\'' + ", prefix='"
                + prefix + '\'' + ", fileExtension='" + fileExtension + '\''
                + ", timeout=" + timeout + ", maxRetry='" + maxRetry + '\''
                + ", configLongPollTimeout='" + configLongPollTimeout + '\''
                + ", configRetryTime='" + configRetryTime + '\''
                + ", enableRemoteSyncConfig=" + enableRemoteSyncConfig + ", endpoint='"
                + endpoint + '\'' + ", namespace='" + namespace + '\'' + ", accessKey='"
                + accessKey + '\'' + ", secretKey='" + secretKey + '\''
                + ", ramRoleName='" + ramRoleName + '\'' + ", contextPath='" + contextPath
                + '\'' + ", clusterName='" + clusterName + '\'' + ", name='" + name + '\''
                + '\'' + ", shares=" + sharedConfigs + ", extensions=" + extensionConfigs
                + ", refreshEnabled=" + refreshEnabled + '}';
    }

    public static class Config {
        private String dataId;
        private String group = "DEFAULT_GROUP";
        private boolean refresh = false;

        public Config() {}

        public Config(String dataId) {
            this.dataId = dataId;
        }

        public Config(String dataId, String group) {
            this(dataId);
            this.group = group;
        }

        public Config(String dataId, boolean refresh) {
            this(dataId);
            this.refresh = refresh;
        }

        public Config(String dataId, String group, boolean refresh) {
            this(dataId, group);
            this.refresh = refresh;
        }

        public String getDataId() {
            return dataId;
        }

        public Config setDataId(String dataId) {
            this.dataId = dataId;
            return this;
        }

        public String getGroup() {
            return group;
        }

        public Config setGroup(String group) {
            this.group = group;
            return this;
        }

        public boolean isRefresh() {
            return refresh;
        }

        public Config setRefresh(boolean refresh) {
            this.refresh = refresh;
            return this;
        }

        @Override
        public String toString() {
            return "Config{" + "dataId='" + dataId + '\'' + ", group='" + group + '\''
                    + ", refresh=" + refresh + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;

            if (o == null || getClass() != o.getClass())
                return false;

            Config config = (Config) o;
            return refresh == config.refresh && Objects.equals(dataId, config.dataId)
                    && Objects.equals(group, config.group);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dataId, group, refresh);
        }
    }
}
