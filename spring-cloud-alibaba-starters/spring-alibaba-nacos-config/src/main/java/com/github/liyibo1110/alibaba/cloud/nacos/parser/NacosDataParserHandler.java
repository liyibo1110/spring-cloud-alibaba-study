package com.github.liyibo1110.alibaba.cloud.nacos.parser;

import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 根据配置格式后缀，选择合适的PropertySourceLoader，把Nacos返回的配置文本，解析成Spring的PropertySource。
 * @author liyibo
 * @date 2026-06-01 10:58
 */
public class NacosDataParserHandler {

    private static final String DEFAULT_EXTENSION = "properties";

    /** PropertySourceLoader接口负责把某种格式的配置文件内容，解析成PropertySource，例如PropertiesPropertySourceLoader和YamlPropertySourceLoader */
    private static List<PropertySourceLoader> propertySourceLoaders;

    private NacosDataParserHandler() {
        // 直接复用Spring Boot的配置解析体系
        propertySourceLoaders = SpringFactoriesLoader.loadFactories(PropertySourceLoader.class, getClass().getClassLoader());
    }

    /**
     *
     * @param configName 配置名称，类似DEFAULT_GROUP@user-service.yml这样
     * @param configValue Nacos返回的原始配置文本
     * @param extension 配置文件后缀
     */
    public List<PropertySource<?>> parseNacosData(String configName, String configValue, String extension) throws IOException {
        if (!StringUtils.hasLength(configValue))
            return Collections.emptyList();

        // 如果没传extension，则从配置名称来推断
        if (!StringUtils.hasLength(extension))
            extension = this.getFileExtension(configName);

        // 遍历所有PropertySourceLoader，找到能处理当前extension的loader
        for (PropertySourceLoader propertySourceLoader : propertySourceLoaders) {
            if (!canLoadFileExtension(propertySourceLoader, extension))
                continue;

        }

        return Collections.emptyList();
    }

    /**
     * 检查给定的扩展名能否被处理。
     */
    private boolean canLoadFileExtension(PropertySourceLoader loader, String extension) {
        return Arrays.stream(loader.getFileExtensions())
                .anyMatch(ext -> StringUtils.endsWithIgnoreCase(extension, ext));
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension(String name) {
        if (!StringUtils.hasLength(name))
            return DEFAULT_EXTENSION;

        int idx = name.lastIndexOf(DOT);
        if (idx > 0 && idx < name.length() - 1)
            return name.substring(idx + 1);

        return DEFAULT_EXTENSION;
    }

    /**
     * 获取文件名
     */
    private String getFileName(String name, String extension) {
        if (!StringUtils.hasLength(extension))
            return name;

        if (!StringUtils.hasLength(name))
            return extension;

        int idx = name.lastIndexOf(DOT);
        if (idx > 0 && idx < name.length() - 1) {
            String ext = name.substring(idx + 1);
            if (extension.equalsIgnoreCase(ext))
                return name;
        }
        return name + DOT + extension;
    }

    public static NacosDataParserHandler getInstance() {
        return ParserHandler.HANDLER;
    }

    private static class ParserHandler {
        private static final NacosDataParserHandler HANDLER = new NacosDataParserHandler();

    }
}
