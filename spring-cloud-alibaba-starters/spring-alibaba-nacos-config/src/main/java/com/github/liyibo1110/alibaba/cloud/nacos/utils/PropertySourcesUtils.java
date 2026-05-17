package com.github.liyibo1110.alibaba.cloud.nacos.utils;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * PropertySources相关工具类。
 * @author liyibo
 * @date 2026-05-17 12:54
 */
public final class PropertySourcesUtils {

    private PropertySourcesUtils() {}

    /**
     * Empty String array.
     */
    public static final String[] EMPTY_STRING_ARRAY = {};

    /**
     * Get Sub {@link Properties}.
     *
     * @param propertySources {@link PropertySource} Iterable.
     * @param prefix          the prefix of property name.
     * @return Map
     * @see Properties
     */
    public static Map<String, Object> getSubProperties(Iterable<PropertySource<?>> propertySources, String prefix) {

        MutablePropertySources mutablePropertySources = new MutablePropertySources();

        for (PropertySource<?> source : propertySources) {
            mutablePropertySources.addLast(source);
        }

        return getSubProperties(mutablePropertySources, prefix);

    }

    /**
     * Get Sub {@link Properties}.
     *
     * @param environment {@link ConfigurableEnvironment}.
     * @param prefix      the prefix of property name.
     * @return Map
     * @see Properties
     */
    public static Map<String, Object> getSubProperties(ConfigurableEnvironment environment, String prefix) {

        return getSubProperties(environment.getPropertySources(), environment, prefix);
    }

    /**
     * Normalize the prefix.
     *
     * @param prefix the prefix.
     * @return the prefix.
     */
    public static String normalizePrefix(String prefix) {
        return prefix.endsWith(".") ? prefix : prefix + ".";
    }

    /**
     * Get prefixed {@link Properties}.
     *
     * @param propertySources {@link PropertySources}.
     * @param prefix          the prefix of property name.
     * @return Map
     * @see Properties
     */
    public static Map<String, Object> getSubProperties(PropertySources propertySources, String prefix) {

        return getSubProperties(propertySources, new PropertySourcesPropertyResolver(propertySources), prefix);
    }

    /**
     * Get prefixed {@link Properties}.
     *
     * @param propertySources  {@link PropertySources}.
     * @param propertyResolver {@link PropertyResolver} to resolve the placeholder if present.
     * @param prefix           the prefix of property name.
     * @return Map
     * @see Properties
     */
    public static Map<String, Object> getSubProperties(PropertySources propertySources, PropertyResolver propertyResolver, String prefix) {

        Map<String, Object> subProperties = new LinkedHashMap<String, Object>();

        String normalizedPrefix = normalizePrefix(prefix);

        for (PropertySource<?> source : propertySources) {
            for (String name : getPropertyNames(source)) {
                if (!subProperties.containsKey(name) && name.startsWith(normalizedPrefix)) {
                    String subName = name.substring(normalizedPrefix.length());
                    if (!subProperties.containsKey(subName)) { // take first one
                        Object value = source.getProperty(name);
                        if (value instanceof String) {
                            // Resolve placeholder
                            value = propertyResolver.resolvePlaceholders((String) value);
                        }
                        subProperties.put(subName, value);
                    }
                }
            }
        }

        return Collections.unmodifiableMap(subProperties);
    }

    /**
     * Get the property names as the array from the specified {@link PropertySource} instance.
     *
     * @param propertySource {@link PropertySource} instance.
     * @return non-null
     */
    public static String[] getPropertyNames(PropertySource propertySource) {

        String[] propertyNames = propertySource instanceof EnumerablePropertySource ?
                ((EnumerablePropertySource<?>) propertySource).getPropertyNames() : null;

        if (propertyNames == null) {
            propertyNames = EMPTY_STRING_ARRAY;
        }

        return propertyNames;
    }
}
