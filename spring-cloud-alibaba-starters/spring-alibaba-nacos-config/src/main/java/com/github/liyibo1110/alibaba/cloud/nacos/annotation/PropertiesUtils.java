package com.github.liyibo1110.alibaba.cloud.nacos.annotation;

import com.alibaba.nacos.common.utils.StringUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

/**
 * Properties相关工具类，只在此package下被使用。
 * @author liyibo
 * @date 2026-05-21 11:40
 */
final class PropertiesUtils {

    private PropertiesUtils() {}

    /**
     * String（properties or yaml） -> java.util.Properties
     */
    public static Properties convertToProperties(String content) throws Exception {
        if (StringUtils.isBlank(content))
            return new Properties();

        try {
            return convertFormYamlContent(content);
        } catch (Exception e) {
            return convertFormPropertiesContent(content);
        }
    }

    /**
     * String -> java.util.Properties
     */
    private static Properties convertFormPropertiesContent(String content) throws Exception {
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        return properties;
    }

    /**
     * String（yaml） -> java.util.Properties
     */
    private static Properties convertFormYamlContent(String content) {
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        Map<String, Object> yamlMap = yaml.load(content);

        Properties properties = new Properties();
        flattenMap("", yamlMap, properties);
        return properties;
    }

    private static void flattenMap(String prefix, Map<String, Object> map, Properties properties) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? String.valueOf(entry.getKey()) : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map)
                flattenMap(key, (Map<String, Object>) entry.getValue(), properties);
            else
                properties.setProperty(key, entry.getValue().toString());
        }
    }
}
