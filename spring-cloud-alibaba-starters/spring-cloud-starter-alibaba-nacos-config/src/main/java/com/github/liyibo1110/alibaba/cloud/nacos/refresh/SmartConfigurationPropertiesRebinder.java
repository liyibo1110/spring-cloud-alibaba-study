package com.github.liyibo1110.alibaba.cloud.nacos.refresh;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.properties.ConfigurationPropertiesBeans;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author liyibo
 * @date 2026-06-09 11:21
 */
public class SmartConfigurationPropertiesRebinder extends ConfigurationPropertiesRebinder {

    private Map<String, ConfigurationPropertiesBean> beanMap;

    private ApplicationContext applicationContext;

    private RefreshBehavior refreshBehavior;

    public SmartConfigurationPropertiesRebinder(ConfigurationPropertiesBeans beans) {
        super(beans);
        fillBeanMap(beans);
    }

    @SuppressWarnings("unchecked")
    private void fillBeanMap(ConfigurationPropertiesBeans beans) {
        this.beanMap = new HashMap<>();
        Field field = ReflectionUtils.findField(beans.getClass(), "beans");
        if (field != null) {
            field.setAccessible(true);
            this.beanMap.putAll((Map<String, ConfigurationPropertiesBean>) Optional
                    .ofNullable(ReflectionUtils.getField(field, beans))
                    .orElse(Collections.emptyMap()));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        super.setApplicationContext(applicationContext);
        this.applicationContext = applicationContext;
        this.refreshBehavior = this.applicationContext.getEnvironment().getProperty(
                "spring.cloud.nacos.config.refresh-behavior", RefreshBehavior.class, RefreshBehavior.ALL_BEANS);
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        if (this.applicationContext.equals(event.getSource()) || event.getKeys().equals(event.getSource())) {
            switch (refreshBehavior) {
                case SPECIFIC_BEAN -> rebindSpecificBean(event);
                default -> rebind();
            }
        }
    }

    private void rebindSpecificBean(EnvironmentChangeEvent event) {
        Set<String> refreshedSet = new HashSet<>();
        beanMap.forEach((name, bean) -> event.getKeys().forEach(changeKey -> {
            String prefix = AnnotationUtils.getValue(bean.getAnnotation()).toString();
            // prevent multiple refresh one ConfigurationPropertiesBean.
            if (changeKey.startsWith(prefix) && refreshedSet.add(name))
                rebind(name);
        }));
    }
}
