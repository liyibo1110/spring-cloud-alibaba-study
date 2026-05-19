package com.github.liyibo1110.alibaba.cloud.nacos;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-05-19 18:18
 */
@Configuration(proxyBeanMethods = false)
@Conditional(NacosConfigEnabledCondition.class)
public class NacosConfigAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = NacosConfigProperties.class, search = SearchStrategy.CURRENT)
    public NacosConfigProperties nacosConfigProperties(ApplicationContext context) {
        if (context.getParent() != null && BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context.getParent(), NacosConfigProperties.class).length > 0) {
            return BeanFactoryUtils.beanOfTypeIncludingAncestors(context.getParent(), NacosConfigProperties.class);
        }

        if (NacosConfigManager.getInstance() == null) // this should never happen except for some unit tests
            return new NacosConfigProperties();
        else
            return NacosConfigManager.getInstance().getNacosConfigProperties();
    }

    @Bean
    public NacosRefreshHistory nacosRefreshHistory() {
        return new NacosRefreshHistory();
    }

    @Bean
    public NacosConfigManager nacosConfigManager(NacosConfigProperties nacosConfigProperties) {
        return NacosConfigManager.getInstance(nacosConfigProperties);
    }

    @Bean
    public static NacosAnnotationProcessor nacosAnnotationProcessor() {
        return new NacosAnnotationProcessor();
    }

    @Bean
    public NacosContextRefresher nacosContextRefresher(NacosConfigManager nacosConfigManager,
                                                       NacosRefreshHistory nacosRefreshHistory) {
        return new NacosContextRefresher(nacosConfigManager, nacosRefreshHistory);
    }
}
