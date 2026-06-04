package com.github.liyibo1110.alibaba.cloud.nacos.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 加载Nacos JSON格式的Loader实现
 * @author liyibo
 * @date 2026-06-03 15:00
 */
public class NacosJsonPropertySourceLoader extends AbstractPropertySourceLoader {

    private static final String VALUE = "value";

    @Override
    public String[] getFileExtensions() {
        return new String[] { "json" };
    }

    @Override
    protected List<PropertySource<?>> doLoad(String name, Resource resource) throws IOException {
        Map<String, Object> result = new LinkedHashMap<>(32);
        ObjectMapper mapper = new ObjectMapper();
        // [fix issue #3043] support comment in json config
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        Map<String, Object> nacosDataMap = mapper.readValue(resource.getInputStream(), LinkedHashMap.class);
        flattenedMap(result, nacosDataMap, null);
        return Collections.singletonList(new OriginTrackedMapPropertySource(name, reloadMap(result), true));
    }

    protected Map<String, Object> reloadMap(Map<String, Object> map) {
        if (map == null || map.isEmpty())
            return null;

        Map<String, Object> result = new LinkedHashMap<>(map);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.contains(AbstractPropertySourceLoader.DOT)) {
                int idx = key.lastIndexOf(AbstractPropertySourceLoader.DOT);
                String suffix = key.substring(idx + 1);
                if (VALUE.equalsIgnoreCase(suffix))
                    result.put(key.substring(0, idx), entry.getValue());
            }
        }
        return result;
    }
}
