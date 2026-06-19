package com.github.liyibo1110.alibaba.cloud.nacos;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * 代表一个特定的服务实例，注意一个服务可以有多个这样的实例。
 * @author liyibo
 * @date 2026-06-18 10:35
 */
public class NacosServiceInstance implements ServiceInstance {

    private String serviceId;

    private String instanceId;

    private String host;

    private int port;

    private boolean secure;

    private Map<String, String> metadata;

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String getScheme() {
        return getUri().getScheme();
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        NacosServiceInstance that = (NacosServiceInstance) o;

        return Objects.equals(this.serviceId, that.serviceId)
                && Objects.equals(this.instanceId, that.instanceId)
                && Objects.equals(this.host, that.host)
                && this.port == that.port
                && this.secure == that.secure
                && Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return (instanceId == null) ? 31 : (instanceId.hashCode() + 31);
    }
}
