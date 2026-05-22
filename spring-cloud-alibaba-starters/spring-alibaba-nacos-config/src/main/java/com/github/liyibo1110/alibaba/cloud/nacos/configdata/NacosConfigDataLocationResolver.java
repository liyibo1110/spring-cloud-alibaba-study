package com.github.liyibo1110.alibaba.cloud.nacos.configdata;

import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigManager;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigProperties;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosPropertiesPrefixer;
import com.github.liyibo1110.alibaba.cloud.nacos.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负责把spring.config.import=nacos:user-service.yml这种配置位置，生成NacosConfigDataResource对象。
 * 属于Spring Boot ConfigDataLocationResolver体系中的Nacos location解析器。
 * @author liyibo
 * @date 2026-05-22 10:26
 */
public class NacosConfigDataLocationResolver implements ConfigDataLocationResolver<NacosConfigDataResource>, Ordered {

    public static final String PREFIX = "nacos:";
    private static final String GROUP = "group";

    /** 支持的参数 */
    private static final String REFRESH_ENABLED = "refreshEnabled";
    private static final String PREFERENCE = "preference";
    private final Log log;

    public NacosConfigDataLocationResolver(DeferredLogFactory logFactory) {
        this.log = logFactory.getLog(getClass());
    }

    /**
     * 尽量靠前执行。
     */
    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 从当前启动环境里，绑定出NacosConfigProperties。
     */
    protected NacosConfigProperties loadProperties(ConfigDataLocationResolverContext context) {
        Binder binder = context.getBinder();
        BindHandler bindHandler = getBindHandler(context);

        NacosConfigProperties nacosConfigProperties;
        if (context.getBootstrapContext().isRegistered(NacosConfigDataLoadProperties.class)) {
            nacosConfigProperties = context.getBootstrapContext().get(NacosConfigDataLoadProperties.class);
        } else {
            String nacosPrefix = NacosPropertiesPrefixer.getPrefix(context.getBinder());

            String nacosConfigPrefix = nacosPrefix + ".config";

            nacosConfigProperties = binder.bind(nacosPrefix, Bindable.of(NacosConfigDataLoadProperties.class), bindHandler)
                    .map(properties -> binder.bind(nacosConfigPrefix, Bindable.ofInstance(properties), bindHandler)
                            .orElse(properties))
                    .orElseGet(() -> binder.bind(nacosConfigPrefix, Bindable.of(NacosConfigDataLoadProperties.class), bindHandler)
                            .orElseGet(NacosConfigDataLoadProperties::new));
        }

        return nacosConfigProperties;
    }

    private BindHandler getBindHandler(ConfigDataLocationResolverContext context) {
        return context.getBootstrapContext().getOrElse(BindHandler.class, null);
    }

    protected Log getLog() {
        return this.log;
    }

    /**
     * 判断这个location，是否可以让我处理。
     * 必须以nacos:为前缀。
     */
    @Override
    public boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
        if (!location.hasPrefix(getPrefix()))
            return false;

        String prefix = NacosPropertiesPrefixer.getPrefix(context.getBinder());
        return context.getBinder().bind(prefix + ".config.enabled", Boolean.class).orElse(true);
    }

    protected String getPrefix() {
        return PREFIX;
    }

    @Override
    public List<NacosConfigDataResource> resolve(ConfigDataLocationResolverContext context, ConfigDataLocation location)
            throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException {
        return Collections.emptyList();
    }

    /**
     * 核心方法
     */
    @Override
    public List<NacosConfigDataResource> resolveProfileSpecific(ConfigDataLocationResolverContext resolverContext,
                                                                ConfigDataLocation location, Profiles profiles) throws ConfigDataLocationNotFoundException {
        // 1、读取并绑定NacosConfigProperties
        NacosConfigProperties properties = loadProperties(resolverContext);
        // 2、取出Spring Boot启动早期的临时上下文
        ConfigurableBootstrapContext bootstrapContext = resolverContext.getBootstrapContext();
        // 3、把NacosConfigProperties注册到bootstrapContext
        bootstrapContext.registerIfAbsent(NacosConfigProperties.class, BootstrapRegistry.InstanceSupplier.of(properties));
        // 4、注册NacosConfigManager
        registerConfigManager(properties, bootstrapContext);
        // 5、根据location、profiles、properties创建NacosConfigDataResource
        return loadConfigDataResources(location, profiles, properties);
    }

    private List<NacosConfigDataResource> loadConfigDataResources(ConfigDataLocation location,
                                                                  Profiles profiles,
                                                                  NacosConfigProperties properties) {
        List<NacosConfigDataResource> result = new ArrayList<>();
        URI uri = getUri(location, properties);

        if (StringUtils.isBlank(dataIdFor(uri)))
            throw new IllegalArgumentException("dataId must be specified");

        NacosConfigDataResource resource = new NacosConfigDataResource(properties,
                location.isOptional(), profiles, log,
                new NacosConfigDataResource.NacosItemConfig().setGroup(groupFor(uri, properties))
                        .setDataId(dataIdFor(uri)).setSuffix(suffixFor(uri, properties))
                        .setRefreshEnabled(refreshEnabledFor(uri, properties))
                        .setPreference(preferenceFor(uri)));
        result.add(resource);

        return result;
    }

    /**
     * 从query参数里取preference。
     */
    private String preferenceFor(URI uri) {
        return getQueryMap(uri).get(PREFERENCE);
    }

    /**
     * 把nacos:user-service.yml转成URI。
     */
    private URI getUri(ConfigDataLocation location, NacosConfigProperties properties) {
        String path = location.getNonPrefixedValue(getPrefix());
        if (StringUtils.isBlank(path))
            path = "/";

        if (!path.startsWith("/"))
            path = "/" + path;

        String uri = properties.getServerAddr() + path;
        return getUri(uri);
    }

    private void registerConfigManager(NacosConfigProperties properties,
                                       ConfigurableBootstrapContext bootstrapContext) {
        if (!bootstrapContext.isRegistered(NacosConfigManager.class))
            bootstrapContext.register(NacosConfigManager.class, BootstrapRegistry.InstanceSupplier.of(NacosConfigManager.getInstance(properties)));
    }

    private URI getUri(String uris) {
        if (!uris.startsWith("http://") && !uris.startsWith("https://"))
            uris = "http://" + uris;

        URI uri;
        try {
            uri = new URI(uris);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("illegal URI: " + uris);
        }
        return uri;
    }

    /**
     * 解析group。
     */
    private String groupFor(URI uri, NacosConfigProperties properties) {
        Map<String, String> queryMap = getQueryMap(uri);
        return queryMap.containsKey(GROUP) ? queryMap.get(GROUP) : properties.getGroup();
    }

    /**
     * 解析URI query参数。
     */
    private Map<String, String> getQueryMap(URI uri) {
        String query = uri.getQuery();
        if (StringUtils.isBlank(query))
            return Collections.emptyMap();

        Map<String, String> result = new HashMap<>(4);
        for (String entry : query.split("&")) {
            String[] kv = entry.split("=");
            if (kv.length == 2)
                result.put(kv[0], kv[1]);
        }
        return result;
    }

    /**
     * 解析配置格式后缀。
     */
    private String suffixFor(URI uri, NacosConfigProperties properties) {
        String dataId = dataIdFor(uri);
        if (dataId != null && dataId.contains("."))
            return dataId.substring(dataId.lastIndexOf('.') + 1);

        return properties.getFileExtension();
    }

    /**
     * 解析refreshEnabled
     */
    private boolean refreshEnabledFor(URI uri, NacosConfigProperties properties) {
        Map<String, String> queryMap = getQueryMap(uri);
        return queryMap.containsKey(REFRESH_ENABLED)
                ? Boolean.parseBoolean(queryMap.get(REFRESH_ENABLED))
                : properties.isRefreshEnabled();
    }

    /**
     * 解析dataId。
     */
    private String dataIdFor(URI uri) {
        String path = uri.getPath();
        // notice '/'
        if (path == null || path.length() <= 1)
            return StringUtils.EMPTY;

        String[] parts = path.substring(1).split("/");
        if (parts.length != 1)
            throw new IllegalArgumentException("illegal dataId");

        return parts[0];
    }
}
