package com.github.liyibo1110.alibaba.cloud.nacos.refresh;

import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigManager;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigProperties;
import com.github.liyibo1110.alibaba.cloud.nacos.client.NacosPropertySource;
import com.github.liyibo1110.alibaba.cloud.nacos.client.NacosPropertySourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 负责监听Spring事件，并在收到Nacos刷新事件后，重新加载并替换Environment里的NacosPropertySource组件。
 * 注意这个组件最终只会替换Environment里面的配置，并不会让相关的Bean感知新的配置值。
 * @author liyibo
 * @date 2026-05-31 16:12
 */
public class NacosPropertySourceRefreshListener implements BeanPostProcessor, SmartApplicationListener, ApplicationContextAware {

    private final static Logger log = LoggerFactory.getLogger(NacosPropertySourceRefreshListener.class);

    /** 保存了所有被@ConfigurationProperties的Bean，但是这个字段此版本并没有被用到 */
    private Map<String, ConfigurationPropertiesBean> beans = new HashMap<>();

    private ApplicationContext applicationContext;

    private AtomicBoolean ready = new AtomicBoolean(false);

    NacosConfigManager nacosConfigManager;

    public NacosPropertySourceRefreshListener(NacosConfigManager nacosConfigManager) {
        this.nacosConfigManager = nacosConfigManager;
    }

    /**
     * 在Bean初始化前的钩子，目的不是修改Bean，而是收集@ConfigurationProperties的Bean。
     * 这个实现目前并没有实际的作用，因为beans字段没有被用到。
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ConfigurationPropertiesBean propertiesBean = ConfigurationPropertiesBean.get(applicationContext, bean, beanName);
        if (propertiesBean != null)
            beans.put(beanName, propertiesBean);
        return bean;    // 不处理原来的Bean，原样返回
    }

    /**
     * 来自SmartApplicationListener，是Spring事件监听接口的增强版。
     * 普通ApplicationListener通常是监听一个明确的泛型事件，而SmartApplicationListener可以在运行时判断支持监听的事件类型。
     * 当前实现监听了2种类型：
     * 1、ApplicationReadyEvent
     * 2、NacosConfigRefreshEvent
     */
    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ApplicationReadyEvent.class.isAssignableFrom(eventType) || NacosConfigRefreshEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 收到监听的回调方法实现。
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // 按照具体类型，调用下面2个分支方法
        if (event instanceof ApplicationReadyEvent)
            handle((ApplicationReadyEvent) event);
        else if (event instanceof NacosConfigRefreshEvent)
            handle((NacosConfigRefreshEvent) event);
    }

    public void handle(ApplicationReadyEvent event) {
        this.ready.compareAndSet(false, true);
    }

    public void handle(NacosConfigRefreshEvent event) {
        // 之前必须先收到ApplicationReadyEvent才行
        if (this.ready.get()) {
            // 判断是否存在另一个刷新监听器，某些场景下，可能有Spring Cloud自己的刷新监听器负责处理更完整的刷新逻辑
            if (!applicationContext.containsBean("nacosConfigSpringCloudRefreshEventListener")) {
                log.info("Event received " + event.getEventDesc());

                // 创建NacosPropertySourceBuilder，
                NacosPropertySourceBuilder nacosPropertySourceBuilder = new NacosPropertySourceBuilder(
                        nacosConfigManager.getConfigService(), nacosConfigManager.getNacosConfigProperties().getTimeout());

                // 类似user-service.yml,DEFAULT_GROUP这样的字符串
                String sourceName = String.join(NacosConfigProperties.COMMAS, event.dataId, event.group);
                ConfigurableEnvironment environment = ((ConfigurableApplicationContext) applicationContext).getEnvironment();
                MutablePropertySources target = environment.getPropertySources();
                // 尝试从Environment里找旧的
                PropertySource<?> prevpropertySource = target.get(sourceName);

                // 找到了就替换，注意写死了properties，并没有取suffix
                if (prevpropertySource instanceof NacosPropertySource) {
                    NacosPropertySource newProperSource = nacosPropertySourceBuilder.build(event.getDataId(), event.getGroup(), "properties", ((NacosPropertySource) prevpropertySource).isRefreshable());
                    target.replace(sourceName, newProperSource);
                    log.info("Replace Nacos Property Source : " + sourceName);
                }
            }
        }
    }
}
