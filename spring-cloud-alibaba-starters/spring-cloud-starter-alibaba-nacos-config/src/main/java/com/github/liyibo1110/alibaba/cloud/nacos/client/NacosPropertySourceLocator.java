package com.github.liyibo1110.alibaba.cloud.nacos.client;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.cloud.nacos.client.NacosPropertySourceBuilder;
import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.alibaba.cloud.nacos.refresh.NacosContextRefresher;
import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.List;

/**
 * 是Spring Cloud Bootstrap机制下的Nacos远程配置定位器，负责在应用主上下文启动前，从Nacos拉取一组默认的dataId配置，
 * 并组装成CompositePropertySource返回给Spring Cloud。
 * 属于2.4版本前的旧机制，新机制就是之前的Loader系列，对应2.4版本后的ConfigData。
 * 这个类主要做四件事：
 * 1、从NacosConfigManager获取ConfigService。
 * 2、根据NacosConfigProperties和Environment推导dataIdPrefix。
 * 3、按固定规则加载几个默认的dataId：
 *  - dataIdPrefix
 *  - dataIdPrefix.fileExtension
 *  - dataIdPrefix-profile.fileExtension
 * 4、把这些NacosPropertySource放入CompositePropertySource，最终返回给Spring Cloud。
 *
 * 旧路线：
 * bootstrap.yml
 *         ↓
 * Spring Cloud Bootstrap Context
 *         ↓
 * PropertySourceLocator.locate(Environment)
 *         ↓
 * NacosPropertySourceLocator
 *         ↓
 * NacosPropertySourceBuilder
 *         ↓
 * CompositePropertySource("NACOS")
 *         ↓
 * Environment
 *
 * 核心特点：
 * 1、不需要spring.config.import配置。
 * 2、主要靠spring.cloud.nacos.config.* 属性。
 * 3、默认按应用名称，自动推导dataId。
 * 4、属于Spring Cloud中的旧bootstrap机制。
 *
 * 对应新路线：
 * application.yml
 *         ↓
 * spring.config.import=nacos:xxx.yml
 *         ↓
 * Spring Boot ConfigData
 *         ↓
 * NacosConfigDataLocationResolver
 *         ↓
 * NacosConfigDataLoader
 *         ↓
 * ConfigData
 *         ↓
 * Environment
 *
 * 核心特点：
 * 1、需要spring.config.import。
 * 2、每个nacos:xxx明确声明了dataId，不再推导。
 * 3、属于Spring Boot 2.4版本的新机制。
 *
 * @author liyibo
 * @date 2026-06-10 10:30
 */
@Order(0)
public class NacosPropertySourceLocator implements PropertySourceLocator {
    private static final Logger log = LoggerFactory.getLogger(NacosPropertySourceLocator.class);

    /** 最外层CompositePropertySource的名字 */
    private static final String NACOS_PROPERTY_SOURCE_NAME = "NACOS";

    /** 用来拼profile dataId，例如app-dev.yml */
    private static final String SEP1 = "-";

    /** 用来拼文件后缀，例如app.yml */
    private static final String DOT = ".";

    private NacosPropertySourceBuilder nacosPropertySourceBuilder;

    private NacosConfigProperties nacosConfigProperties;

    private NacosConfigManager nacosConfigManager;

    @Deprecated
    public NacosPropertySourceLocator(NacosConfigProperties nacosConfigProperties) {
        this.nacosConfigProperties = nacosConfigProperties;
    }

    public NacosPropertySourceLocator(NacosConfigManager nacosConfigManager) {
        this.nacosConfigManager = nacosConfigManager;
        this.nacosConfigProperties = nacosConfigManager.getNacosConfigProperties();
    }

    /**
     * 核心方法。
     */
    @Override
    public PropertySource<?> locate(Environment env) {
        // 1、获取ConfigService
        ConfigService configService = nacosConfigManager.getConfigService();

        if (configService == null) {
            log.warn("no instance of config service found, can't load config from nacos");
            return null;
        }

        // 2、创建NacosPropertySourceBuilder
        long timeout = nacosConfigProperties.getTimeout();
        nacosPropertySourceBuilder = new NacosPropertySourceBuilder(configService, timeout);
        String name = nacosConfigProperties.getName();

        /**
         * 3、确定dataIdPrefix，优先级由高到低：
         *  - spring.cloud.nacos.config.prefix
         *  - spring.cloud.nacos.config.name
         *  - spring.applicaiton.name
         * 意思就是如果没有配置prefix和name，默认会用应用name来做dataId的前缀
         */
        String dataIdPrefix = nacosConfigProperties.getPrefix();
        if (StringUtils.isEmpty(dataIdPrefix))
            dataIdPrefix = name;

        if (StringUtils.isEmpty(dataIdPrefix))
            dataIdPrefix = env.getProperty("spring.application.name");

        // 4、创建CompositePropertySource，因为每个dataId都会生成一个NacosPropertySource，最终统一放到一个叫NACOS的组合里
        CompositePropertySource composite = new CompositePropertySource(NACOS_PROPERTY_SOURCE_NAME);

        // 5、加载应用配置，这里面会真正按规则加载Nacos dataId，最终Bootstrap会把这个composite放入Environment
        loadApplicationConfiguration(composite, dataIdPrefix, nacosConfigProperties, env);
        return composite;
    }

    /**
     * 负责加载三类配置，越后面的优先级越高，因为是后面覆盖前面的。
     */
    private void loadApplicationConfiguration(CompositePropertySource compositePropertySource, String dataIdPrefix,
                                              NacosConfigProperties properties, Environment environment) {
        String fileExtension = properties.getFileExtension();
        String nacosGroup = properties.getGroup();
        // 第一类：不带文件后缀的dataId
        loadNacosDataIfPresent(compositePropertySource, dataIdPrefix, nacosGroup, fileExtension, true);
        // 第二类：带文件后缀的dataId
        loadNacosDataIfPresent(compositePropertySource, dataIdPrefix + DOT + fileExtension, nacosGroup, fileExtension, true);
        // 第三类：带profile的dataId
        for (String profile : environment.getActiveProfiles()) {
            String dataId = dataIdPrefix + SEP1 + profile + DOT + fileExtension;
            loadNacosDataIfPresent(compositePropertySource, dataId, nacosGroup, fileExtension, true);
        }
    }

    private void loadNacosConfiguration(final CompositePropertySource composite, List<NacosConfigProperties.Config> configs) {
        for (NacosConfigProperties.Config config : configs)
            loadNacosDataIfPresent(composite, config.getDataId(), config.getGroup(), NacosDataParserHandler.getInstance().getFileExtension(config.getDataId()), config.isRefresh());
    }

    /**
     * 检验配置列表里的每个元素，必须有dataId
     */
    private void checkConfiguration(List<NacosConfigProperties.Config> configs, String tips) {
        for (int i = 0; i < configs.size(); i++) {
            String dataId = configs.get(i).getDataId();
            if (dataId == null || dataId.trim().length() == 0)
                throw new IllegalStateException(String.format("the [ spring.cloud.nacos.config.%s[%s] ] must give a dataId", tips, i));
        }
    }

    private void loadNacosDataIfPresent(final CompositePropertySource composite,
                                        final String dataId, final String group, String fileExtension,
                                        boolean isRefreshable) {
        if (null == dataId || dataId.trim().length() < 1)
            return;

        if (null == group || group.trim().length() < 1)
            return;

        NacosPropertySource propertySource = this.loadNacosPropertySource(dataId, group, fileExtension, isRefreshable);
        this.addFirstPropertySource(composite, propertySource, false);
    }

    private NacosPropertySource loadNacosPropertySource(final String dataId, final String group,
                                                        String fileExtension, boolean isRefreshable) {
        if (NacosContextRefresher.getRefreshCount() != 0) {
            if (!isRefreshable)
                return NacosPropertySourceRepository.getNacosPropertySource(dataId, group);
        }
        return nacosPropertySourceBuilder.build(dataId, group, fileExtension, isRefreshable);
    }

    /**
     * 把单个Nacos配置，放到composite里。
     */
    private void addFirstPropertySource(final CompositePropertySource composite, NacosPropertySource nacosPropertySource,
                                        boolean ignoreEmpty) {
        if (null == nacosPropertySource || null == composite)
            return;

        if (ignoreEmpty && nacosPropertySource.getSource().isEmpty())
            return;

        composite.addFirstPropertySource(nacosPropertySource);
    }

    public void setNacosConfigManager(NacosConfigManager nacosConfigManager) {
        this.nacosConfigManager = nacosConfigManager;
    }
}
