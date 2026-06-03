package com.github.liyibo1110.alibaba.cloud.nacos.parser;

import com.github.liyibo1110.alibaba.cloud.nacos.utils.NacosConfigUtils;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

            NacosByteArrayResource nacosByteArrayResource;
            // 如果是要解析properties文件，则要额外转成unicode，因为PropertiesPropertySourceLoader内部使用的是ISO_8859_1，中文会乱码
            if(propertySourceLoader instanceof PropertiesPropertySourceLoader)
                nacosByteArrayResource = new NacosByteArrayResource(NacosConfigUtils.selectiveConvertUnicode(configValue).getBytes(), configName);
            else
                nacosByteArrayResource = new NacosByteArrayResource(configValue.getBytes(), configName);

            nacosByteArrayResource.setFilename(getFileName(configName, extension));
            // 正式调用特定loader的load
            List<PropertySource<?>> propertySourceList = propertySourceLoader.load(configName, nacosByteArrayResource);
            if (CollectionUtils.isEmpty(propertySourceList))
                return Collections.emptyList();
            /**
             * 后半段的重要处理，就是把EnumerablePropertySource转成OriginTrackedMapPropertySource。
             *
             * 即如果是EnumerablePropertySource，则取出所有propertyNames，然后获取值，放到LinkedHashMap中，用来保留有顺序的属性名，
             * 最后重新包装成OriginTrackedMapPropertySource。
             *
             * 目的是更适合Spring Boot的配置体系使用，并且支持origin tracking的相关能力。
             */
            return propertySourceList.stream().filter(Objects::nonNull)
                .map(propertySource -> {
                    if (propertySource instanceof EnumerablePropertySource enumerablePropertySource) {
                        String[] propertyNames = enumerablePropertySource.getPropertyNames();
                        if (propertyNames != null && propertyNames.length > 0) {
                            Map<String, Object> map = new LinkedHashMap<>();
                            Arrays.stream(propertyNames).forEach(name -> {
                                map.put(name, propertySource.getProperty(name));
                            });
                            return new OriginTrackedMapPropertySource(propertySource.getName(), map, true);
                        }
                    }
                    return propertySource;
                }).collect(Collectors.toList());
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
