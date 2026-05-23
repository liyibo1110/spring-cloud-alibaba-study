package com.github.liyibo1110.alibaba.cloud.nacos.configdata;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigManager;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigProperties;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosPropertiesPrefixer;
import org.apache.commons.logging.Log;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 负责根据NacosConfigDataResource，真正去Nacos里面拉取配置，并封装成ConfigData。
 * PropertySource -> ConfigData -> Environment
 * @author liyibo
 * @date 2026-05-22 14:38
 */
public class NacosConfigDataLoader implements ConfigDataLoader<NacosConfigDataResource> {

    private final Log log;

    public NacosConfigDataLoader(DeferredLogFactory logFactory) {
        this.log = logFactory.getLog(getClass());
    }

    @Override
    public ConfigData load(ConfigDataLoaderContext context, NacosConfigDataResource resource) {
        return doLoad(context, resource);
    }

    public ConfigData doLoad(ConfigDataLoaderContext context, NacosConfigDataResource resource) {
        try {
            // 1、取NacosConfigManager
            ConfigService configService = getBean(context, NacosConfigManager.class).getConfigService();
            // 2、取NacosConfigProperties
            NacosConfigProperties properties = getBean(context, NacosConfigProperties.class);
            // 3、从Resource里取本次要加载的配置项
            NacosConfigDataResource.NacosItemConfig config = resource.getConfig();
            // 4、拉取远程配置，注意这个PropertySource是Spring本身的
            List<PropertySource<?>> propertySources = pullConfig(configService,
                    config.getGroup(), config.getDataId(), config.getSuffix(),
                    properties.getTimeout());
            // 5、构造NacosPropertySource
            NacosPropertySource propertySource = new NacosPropertySource(propertySources,
                    config.getGroup(), config.getDataId(), new Date(),
                    config.isRefreshEnabled());
            // 6、收集到NacosPropertySourceRepository，和动态刷新有关，涉及了触发listener
            NacosPropertySourceRepository.collectNacosPropertySource(propertySource);
            // 7、最终返回ConfigData
            return new ConfigData(propertySources, getOptions(context, resource));
        } catch (Exception e) {
            log.error("Error getting properties from nacos: " + resource, e);
            // 没有开启optional为true，则会抛异常，否则吃异常
            if (!resource.isOptional())
                throw new ConfigDataResourceNotFoundException(resource, e);
        }
        return null;
    }

    /**
     * 控制ConfigData的加载选项。
     */
    private ConfigData.Option[] getOptions(ConfigDataLoaderContext context, NacosConfigDataResource resource) {
        List<ConfigData.Option> options = new ArrayList<>();
        options.add(ConfigData.Option.IGNORE_IMPORTS);  // 配置里如果也包含spring.config.import，则不要处理里面这个，避免递归
        options.add(ConfigData.Option.IGNORE_PROFILES); // 忽略从这份ConfigData中激活的profile的能力
        if (getPreference(context, resource) == ConfigPreference.REMOTE) {
            // mark it as 'PROFILE_SPECIFIC' config, it has higher priority,
            // will override the none profile specific config.
            // fixed https://github.com/alibaba/spring-cloud-alibaba/issues/2455
            options.add(ConfigData.Option.PROFILE_SPECIFIC);
        }
        return options.toArray(new ConfigData.Option[0]);
    }

    /**
     * 决定是本地优先，还是远程优先，默认本地优先。
     */
    private ConfigPreference getPreference(ConfigDataLoaderContext context, NacosConfigDataResource resource) {
        Binder binder = context.getBootstrapContext().get(Binder.class);
        String prefix = NacosPropertiesPrefixer.getPrefix(binder);
        // 1、获取全局preference
        ConfigPreference preference = binder.bind(prefix + ".config.preference", ConfigPreference.class).orElse(ConfigPreference.LOCAL);
        // 2、单个import的preference
        String specificPreference = resource.getConfig().getPreference();
        if (specificPreference != null) {
            try {
                preference = ConfigPreference.valueOf(specificPreference.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignore) {
                // illegal preference value, just ignore.
                log.error(String.format("illegal preference value: %s, using default preference: %s", specificPreference, preference));
            }
        }
        return preference;
    }

    /**
     * 真正访问Nacos，并解析配置文本。
     */
    private List<PropertySource<?>> pullConfig(ConfigService configService, String group,
                                               String dataId, String suffix, long timeout) throws NacosException, IOException {
        // 1、从Nacos Server拉取配置文本
        String config = configService.getConfig(dataId, group, timeout);
        logLoadInfo(group, dataId, config);
        // fixed issue: https://github.com/alibaba/spring-cloud-alibaba/issues/2906 .
        String configName = group + "@" + dataId;
        // 2、根据suffix解析配置文本，生成PropertySource列表
        return NacosDataParserHandler.getInstance().parseNacosData(configName, config, suffix);
    }

    private void logLoadInfo(String group, String dataId, String config) {
        if (config != null)
            log.info(String.format("[Nacos Config] Load config[dataId=%s, group=%s] success", dataId, group));
        else
            log.warn(String.format("[Nacos Config] config[dataId=%s, group=%s] is empty", dataId, group));

        if (log.isDebugEnabled())
            log.debug(String.format("[Nacos Config] config[dataId=%s, group=%s] content: \n%s", dataId, group, config));
    }

    /**
     * 从BootstrapContext中取对象，注意不是从Spring的Bean容器里面取，因为这时Bean容器还未准备就绪。
     */
    protected <T> T getBean(ConfigDataLoaderContext context, Class<T> type) {
        if (context.getBootstrapContext().isRegistered(type))
            return context.getBootstrapContext().get(type);
        return null;
    }
}
