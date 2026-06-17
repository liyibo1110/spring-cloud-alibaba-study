package com.github.liyibo1110.alibaba.cloud.nacos.registry;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import java.util.List;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * 负责把Spring Cloud的register / deregister / setStatus / getStatus等标准方法，桥接成对Nacos NamingService的调用。
 * @author liyibo
 * @date 2026-06-16 10:40
 */
public class NacosServiceRegistry implements ServiceRegistry<Registration> {

    private static final Logger log = LoggerFactory.getLogger(NacosServiceRegistry.class);

    private static final String STATUS_UP = "UP";

    private static final String STATUS_DOWN = "DOWN";

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    private final NacosServiceManager nacosServiceManager;

    public NacosServiceRegistry(NacosServiceManager nacosServiceManager, NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
        this.nacosServiceManager = nacosServiceManager;
    }

    @Override
    public void register(Registration registration) {
        if (StringUtils.isEmpty(registration.getServiceId())) {
            log.warn("No service to register for nacos client...");
            return;
        }

        NamingService namingService = namingService();
        String serviceId = registration.getServiceId();
        String group = nacosDiscoveryProperties.getGroup();

        Instance instance = getNacosInstanceFromRegistration(registration);

        try {
            namingService.registerInstance(serviceId, group, instance);
            log.info("nacos registry, {} {} {}:{} register finished", group, serviceId, instance.getIp(), instance.getPort());
        } catch (Exception e) {
            if (nacosDiscoveryProperties.isFailFast()) {
                log.error("nacos registry, {} register failed...{},", serviceId, registration.toString(), e);
                rethrowRuntimeException(e);
            } else {
                log.warn("Failfast is false. {} register failed...{},", serviceId, registration.toString(), e);
            }
        }
    }

    @Override
    public void deregister(Registration registration) {
        log.info("De-registering from Nacos Server now...");

        if (StringUtils.isEmpty(registration.getServiceId())) {
            log.warn("No dom to de-register for nacos client...");
            return;
        }

        NamingService namingService = namingService();
        String serviceId = registration.getServiceId();
        String group = nacosDiscoveryProperties.getGroup();

        try {
            namingService.deregisterInstance(serviceId, group, registration.getHost(), registration.getPort(), nacosDiscoveryProperties.getClusterName());
        } catch (Exception e) {
            log.error("ERR_NACOS_DEREGISTER, de-register failed...{},", registration.toString(), e);
        }

        log.info("De-registration finished.");
    }

    @Override
    public void close() {
        try {
            nacosServiceManager.nacosServiceShutDown();
        } catch (NacosException e) {
            log.error("Nacos namingService shutDown failed", e);
        }
    }

    @Override
    public void setStatus(Registration registration, String status) {
        if (!STATUS_UP.equalsIgnoreCase(status) && !STATUS_DOWN.equalsIgnoreCase(status)) {
            log.warn("can't support status {},please choose UP or DOWN", status);
            return;
        }

        String serviceId = registration.getServiceId();

        Instance instance = getNacosInstanceFromRegistration(registration);

        if (STATUS_DOWN.equalsIgnoreCase(status))
            instance.setEnabled(false);
        else
            instance.setEnabled(true);

        try {
            nacosServiceManager.getNamingService().registerInstance(serviceId, nacosDiscoveryProperties.getGroup(), instance);
        } catch (Exception e) {
            throw new RuntimeException("update nacos instance status fail", e);
        }
    }

    @Override
    public Object getStatus(Registration registration) {
        String serviceName = registration.getServiceId();
        String group = nacosDiscoveryProperties.getGroup();
        try {
            List<Instance> instances = namingService().getAllInstances(serviceName, group);
            for (Instance instance : instances) {
                if (instance.getIp().equalsIgnoreCase(nacosDiscoveryProperties.getIp())
                        && instance.getPort() == nacosDiscoveryProperties.getPort()) {
                    return instance.isEnabled() ? STATUS_UP : STATUS_DOWN;
                }
            }
        } catch (Exception e) {
            log.error("get all instance of {} error,", serviceName, e);
        }
        return null;
    }

    private Instance getNacosInstanceFromRegistration(Registration registration) {
        Instance instance = new Instance();
        instance.setIp(registration.getHost());
        instance.setPort(registration.getPort());
        instance.setWeight(nacosDiscoveryProperties.getWeight());
        instance.setClusterName(nacosDiscoveryProperties.getClusterName());
        instance.setEnabled(nacosDiscoveryProperties.isInstanceEnabled());
        instance.setMetadata(registration.getMetadata());
        instance.setEphemeral(nacosDiscoveryProperties.isEphemeral());
        return instance;
    }

    private NamingService namingService() {
        return nacosServiceManager.getNamingService();
    }
}
