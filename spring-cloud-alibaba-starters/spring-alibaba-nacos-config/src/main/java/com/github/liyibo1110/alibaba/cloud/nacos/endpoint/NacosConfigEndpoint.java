package com.github.liyibo1110.alibaba.cloud.nacos.endpoint;

import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigProperties;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.github.liyibo1110.alibaba.cloud.nacos.client.NacosPropertySource;
import com.github.liyibo1110.alibaba.cloud.nacos.refresh.NacosRefreshHistory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nacos专用的Endpoint，包括properties和refresh history组件
 * @author liyibo
 * @date 2026-06-04 10:48
 */
@Endpoint(id = "nacosconfig")
public class NacosConfigEndpoint {

    private final NacosConfigProperties properties;

    private final NacosRefreshHistory refreshHistory;

    private ThreadLocal<DateFormat> dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public NacosConfigEndpoint(NacosConfigProperties properties, NacosRefreshHistory refreshHistory) {
        this.properties = properties;
        this.refreshHistory = refreshHistory;
    }

    @ReadOperation
    public Map<String, Object> invoke() {
        Map<String, Object> result = new HashMap<>(16);
        result.put("NacosConfigProperties", properties);

        List<NacosPropertySource> all = NacosPropertySourceRepository.getAll();

        List<Map<String, Object>> sources = new ArrayList<>();
        for (NacosPropertySource ps : all) {
            Map<String, Object> source = new HashMap<>(16);
            source.put("dataId", ps.getDataId());
            source.put("lastSynced", dateFormat.get().format(ps.getTimestamp()));
            sources.add(source);
        }
        result.put("Sources", sources);
        result.put("RefreshHistory", refreshHistory.getRecords());

        return result;
    }
}
