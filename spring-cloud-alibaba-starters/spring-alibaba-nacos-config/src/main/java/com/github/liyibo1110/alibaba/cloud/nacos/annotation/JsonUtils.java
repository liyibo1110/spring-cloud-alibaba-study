package com.github.liyibo1110.alibaba.cloud.nacos.annotation;

import com.alibaba.nacos.api.exception.runtime.NacosDeserializationException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * JSON相关工具类，只在此package下被使用。
 * @author liyibo
 * @date 2026-05-21 11:32
 */
final class JsonUtils {

    private JsonUtils() {}

    static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 反序列化。
     */
    public static <T> T toObj(String json, Class<T> cls) {
        try {
            return mapper.readValue(json, cls);
        } catch (IOException e) {
            throw new NacosDeserializationException(cls, e);
        }
    }

    /**
     * 反序列化。
     */
    public static <T> T toObj(String json, Type type) {
        try {
            return mapper.readValue(json, TypeFactory.defaultInstance().constructType(type));
        } catch (IOException e) {
            throw new NacosDeserializationException(type, e);
        }
    }
}
