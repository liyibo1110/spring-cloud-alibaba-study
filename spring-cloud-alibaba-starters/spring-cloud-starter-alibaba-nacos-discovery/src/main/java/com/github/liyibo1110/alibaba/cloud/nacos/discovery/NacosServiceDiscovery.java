package com.github.liyibo1110.alibaba.cloud.nacos.discovery;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosServiceInstance;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosServiceManager;
import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 封装了NacosServiceManager组件（内部封装了最重要的NamingService和NamingMaintainService组件），
 * 配合NacosDiscoveryClient来进行服务查询。
 * @author liyibo
 * @date 2026-06-21 13:34
 */
public class NacosServiceDiscovery {

    private NacosDiscoveryProperties discoveryProperties;

    private NacosServiceManager nacosServiceManager;

    public NacosServiceDiscovery(NacosDiscoveryProperties discoveryProperties,
                                 NacosServiceManager nacosServiceManager) {
        this.discoveryProperties = discoveryProperties;
        this.nacosServiceManager = nacosServiceManager;
    }

    public List<ServiceInstance> getInstances(String serviceId) throws NacosException {
        String group = discoveryProperties.getGroup();
        List<Instance> instances = namingService().selectInstances(serviceId, group, true);
        return hostToServiceInstanceList(instances, serviceId);
    }

    public List<String> getServices() throws NacosException {
        String group = discoveryProperties.getGroup();
        ListView<String> services = namingService().getServicesOfServer(1, Integer.MAX_VALUE, group);
        return services.getData();
    }

    public static List<ServiceInstance> hostToServiceInstanceList(List<Instance> instances, String serviceId) {
        List<ServiceInstance> result = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            ServiceInstance serviceInstance = hostToServiceInstance(instance, serviceId);
            if (serviceInstance != null)
                result.add(serviceInstance);
        }
        return result;
    }

    public static ServiceInstance hostToServiceInstance(Instance instance, String serviceId) {
        if (instance == null || !instance.isEnabled() || !instance.isHealthy())
            return null;

        NacosServiceInstance nacosServiceInstance = new NacosServiceInstance();
        nacosServiceInstance.setHost(instance.getIp());
        nacosServiceInstance.setPort(instance.getPort());
        nacosServiceInstance.setServiceId(serviceId);
        nacosServiceInstance.setInstanceId(instance.getInstanceId());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("nacos.instanceId", instance.getInstanceId());
        metadata.put("nacos.weight", instance.getWeight() + "");
        metadata.put("nacos.healthy", instance.isHealthy() + "");
        metadata.put("nacos.cluster", instance.getClusterName() + "");
        if (instance.getMetadata() != null) {
            metadata.putAll(instance.getMetadata());
        }
        metadata.put("nacos.ephemeral", String.valueOf(instance.isEphemeral()));
        nacosServiceInstance.setMetadata(metadata);

        if (metadata.containsKey("secure")) {
            boolean secure = Boolean.parseBoolean(metadata.get("secure"));
            nacosServiceInstance.setSecure(secure);
        }
        return nacosServiceInstance;
    }

    private NamingService namingService() {
        return nacosServiceManager.getNamingService();
    }
}
