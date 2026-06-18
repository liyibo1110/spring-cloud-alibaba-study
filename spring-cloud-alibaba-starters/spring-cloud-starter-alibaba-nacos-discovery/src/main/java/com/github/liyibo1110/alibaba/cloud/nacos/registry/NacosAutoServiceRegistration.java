package com.github.liyibo1110.alibaba.cloud.nacos.registry;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.github.liyibo1110.alibaba.cloud.nacos.event.NacosDiscoveryInfoChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;

/**
 * 应用启动到特定阶段时，自动调用NacosServiceRegistry的register方法，把当前应用注册到Nacos。
 * 因为要成功获取到应用端口才能去注册，因此这个自动触发时机会尽量晚一些。
 * @author liyibo
 * @date 2026-06-17 13:25
 */
public class NacosAutoServiceRegistration extends AbstractAutoServiceRegistration<Registration> {
    private static final Logger log = LoggerFactory.getLogger(NacosAutoServiceRegistration.class);

    private NacosRegistration registration;

    public NacosAutoServiceRegistration(ServiceRegistry<Registration> serviceRegistry,
                                        AutoServiceRegistrationProperties autoServiceRegistrationProperties,
                                        NacosRegistration registration) {
        super(serviceRegistry, autoServiceRegistrationProperties);
        this.registration = registration;
    }

    @Deprecated
    public void setPort(int port) {
        getPort().set(port);
    }

    @Override
    protected NacosRegistration getRegistration() {
        if (this.registration.getPort() < 0 && this.getPort().get() > 0)
            this.registration.setPort(this.getPort().get());

        Assert.isTrue(this.registration.getPort() > 0, "service.port has not been set");
        return this.registration;
    }

    @Override
    protected NacosRegistration getManagementRegistration() {
        return null;
    }

    @Override
    protected void register() {
        if (!this.registration.getNacosDiscoveryProperties().isRegisterEnabled()) {
            log.debug("Registration disabled.");
            return;
        }

        if (this.registration.getPort() < 0)
            this.registration.setPort(getPort().get());

        super.register();
    }

    @Override
    protected void registerManagement() {
        if (!this.registration.getNacosDiscoveryProperties().isRegisterEnabled())
            return;
        super.registerManagement();
    }

    @Override
    protected Object getConfiguration() {
        return this.registration.getNacosDiscoveryProperties();
    }

    @Override
    protected boolean isEnabled() {
        return this.registration.getNacosDiscoveryProperties().isRegisterEnabled();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected String getAppName() {
        String appName = registration.getNacosDiscoveryProperties().getService();
        return StringUtils.isEmpty(appName) ? super.getAppName() : appName;
    }

    @EventListener
    public void onNacosDiscoveryInfoChangedEvent(NacosDiscoveryInfoChangedEvent event) {
        restart();
    }

    private void restart() {
        this.stop();
        this.start();
    }
}
