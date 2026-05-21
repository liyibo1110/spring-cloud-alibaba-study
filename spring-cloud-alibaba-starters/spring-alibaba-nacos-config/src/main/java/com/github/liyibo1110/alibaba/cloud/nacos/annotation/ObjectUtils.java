package com.github.liyibo1110.alibaba.cloud.nacos.annotation;

import org.springframework.util.StringUtils;

import java.lang.reflect.Type;

/**
 * Object相关工具类，只在此package下被使用。
 * @author liyibo
 * @date 2026-05-21 11:33
 */
final class ObjectUtils {

    private ObjectUtils() {}

    /**
     * JSON反序列化。
     */
    public static Object convertToObject(String content, Type clazz) {
        if (!StringUtils.hasText(content))
            return null;
        return convertFormJsonContent(content, clazz);
    }

    /**
     * JSON反序列化。
     */
    private static Object convertFormJsonContent(String content, Type clazz) {
        return JsonUtils.toObj(content, clazz);
    }
}
