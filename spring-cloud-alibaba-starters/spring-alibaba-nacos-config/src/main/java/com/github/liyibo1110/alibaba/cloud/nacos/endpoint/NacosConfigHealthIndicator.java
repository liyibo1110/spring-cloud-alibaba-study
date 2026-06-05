package com.github.liyibo1110.alibaba.cloud.nacos.endpoint;

import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Nacos Config专用的HealthIndicator实现。
 * @author liyibo
 * @date 2026-06-04 10:49
 */
public class NacosConfigHealthIndicator extends AbstractHealthIndicator {

    private final ConfigService configService;

    private final String STATUS_UP = "UP";

    private final String STATUS_DOWN = "DOWN";

    public NacosConfigHealthIndicator(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        // 只会返回UP或DOWN这两种结果
        String status = configService.getServerStatus();
        builder.status(status);
        switch (status) {
            case STATUS_UP -> builder.up();
            case STATUS_DOWN -> builder.down();
            default -> builder.unknown();
        }
    }
}
