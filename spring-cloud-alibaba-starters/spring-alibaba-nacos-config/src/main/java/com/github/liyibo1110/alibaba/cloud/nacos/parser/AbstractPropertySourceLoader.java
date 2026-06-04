package com.github.liyibo1110.alibaba.cloud.nacos.parser;

import com.github.liyibo1110.alibaba.cloud.nacos.utils.StringUtils;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 专门解析json/xml两种格式的loader父类。
 * @author liyibo
 * @date 2026-06-03 13:53
 */
public abstract class AbstractPropertySourceLoader implements PropertySourceLoader {

    static final String DOT = ".";

    /**
     * 相当于一个强制过滤器，不是所有json和xml的内容都可以load，只能load来自NacosByteArrayResource的资源，即Nacos专门的配置。
     */
    protected boolean canLoad(String name, Resource resource) {
        return resource instanceof NacosByteArrayResource;
    }

    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        if (!canLoad(name, resource))
            return Collections.emptyList();
        return doLoad(name, resource);
    }

    /**
     * Resource -> List<PropertySource<?>>
     */
    protected abstract List<PropertySource<?>> doLoad(String name, Resource resource) throws IOException;

    /**
     * 把嵌套结构的Map，展平成Spring Boot能识别的点号key。
     * 例如传入的Map是：
     * server -> { port -> 8081 }
     * spring -> { application -> { name -> user-service } }
     *
     * 但是Spring Environment里需要的是：
     * server.port = 8081
     * spring.application.name = user-service
     */
    protected void flattenedMap(Map<String, Object> result, Map<String, Object> dataMap, String parentKey) {
        if (dataMap == null || dataMap.isEmpty())
            return;

        Set<Map.Entry<String, Object>> entries = dataMap.entrySet();
        for (Iterator<Map.Entry<String, Object>> iterator = entries.iterator(); iterator.hasNext();) {
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();

            String fullKey = StringUtils.isEmpty(parentKey)
                    ? key
                    : key.startsWith("[") ? parentKey.concat(key) : parentKey.concat(DOT).concat(key);

            if (value instanceof Map map) {
                flattenedMap(result, map, fullKey);
                continue;
            } else if (value instanceof Collection collection) {
                int count = 0;
                for (Object object : collection)
                    flattenedMap(result, Collections.singletonMap("[" + (count++) + "]", object), fullKey);

                continue;
            }

            result.put(fullKey, value);
        }
    }
}
