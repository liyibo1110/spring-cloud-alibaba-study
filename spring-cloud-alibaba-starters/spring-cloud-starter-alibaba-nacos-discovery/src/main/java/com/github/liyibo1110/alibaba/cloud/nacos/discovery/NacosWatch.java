package com.github.liyibo1110.alibaba.cloud.nacos.discovery;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 监听当前服务自身，在Nacos注册表中实例变化的生命周期组件。
 * 主要作用是把Nacos服务端里当前实例的metadata变化，同步回本地的NacosDiscoveryProperties。
 * 实现了SmartLifecycle接口，因此会跟随Spring容器生命周期自动启动和销毁。
 * @author liyibo
 * @date 2026-06-22 10:48
 */
public class NacosWatch implements SmartLifecycle, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(NacosWatch.class);

    /**
     * key是user-service:DEFAULT_GROUP这样的字符串，这个Map的主要目的有两个：
     * 1、复用同一个listener对象：因为Nacos的unsubscribe通常需要传入之前subscribe时使用的那个listener，
     * 也就是说不能start的时候new一个listener，stop时候再new一个listener去unsubscribe，否则可能无法取消订阅。
     * 2、防止重复创建监听器：如果service + group组合已经创建过listener，就直接复用。
     **/
    private final Map<String, EventListener> listenerMap = new ConcurrentHashMap<>(16);

    /** 生命周期的状态控制，保证start和stop方法的幂等性。 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final NacosServiceManager nacosServiceManager;

    private final NacosDiscoveryProperties properties;

    public NacosWatch(NacosServiceManager nacosServiceManager, NacosDiscoveryProperties properties) {
        this.nacosServiceManager = nacosServiceManager;
        this.properties = properties;
    }

    /**
     * Spring容器启动时，自动触发start方法。
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public void start() {
        // 1、确保只启动1次
        if (this.running.compareAndSet(false, true)) {
            // 2、构造或复用EventListener
            EventListener eventListener = listenerMap.computeIfAbsent(buildKey(),
                    event -> new EventListener() {
                        @Override
                        public void onEvent(Event event) {
                            // 3、收到Nacos的推送事件了，只处理NameEvent类型，这个事件里有当前服务的实例列表（注意只有当前订阅的这个，不是所有服务的）
                            if (event instanceof NamingEvent namingEvent) {
                                List<Instance> instances = namingEvent.getInstances();
                                // 4、从实例列表中找到自己
                                Optional<Instance> instanceOptional = selectCurrentInstance(instances);
                                // 5、如果找到自己，就同步metadata
                                instanceOptional.ifPresent(currentInstance -> {
                                    resetIfNeeded(currentInstance);
                                });
                            }
                        }
                    });

            NamingService namingService = nacosServiceManager.getNamingService();
            // 6、向Nacos监听，真正的订阅发生在这里
            try {
                namingService.subscribe(properties.getService(), properties.getGroup(),
                        Arrays.asList(properties.getClusterName()), eventListener);
            } catch (Exception e) {
                log.error("namingService subscribe failed, properties:{}", properties, e);
            }

        }
    }

    private String buildKey() {
        return String.join(":", properties.getService(), properties.getGroup());
    }

    /**
     * 这个组件主要干的事情：将远程的metadata，覆盖到本地properties里面的metadata字段里。
     */
    private void resetIfNeeded(Instance instance) {
        if (!properties.getMetadata().equals(instance.getMetadata()))
            properties.setMetadata(instance.getMetadata());
    }

    /**
     * 从特定服务的多个实例列表找到自己，就是根据ip和port找。
     * 不需要再看service / group / cluster之类的了，前面订阅的时候已经限定了这些。
     */
    private Optional<Instance> selectCurrentInstance(List<Instance> instances) {
        return instances.stream()
                .filter(instance -> properties.getIp().equals(instance.getIp())
                        && properties.getPort() == instance.getPort())
                .findFirst();
    }

    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false)) {
            EventListener eventListener = listenerMap.get(buildKey());
            try {
                NamingService namingService = nacosServiceManager.getNamingService();
                namingService.unsubscribe(properties.getService(), properties.getGroup(),
                        Arrays.asList(properties.getClusterName()), eventListener);
            } catch (Exception e) {
                log.error("namingService unsubscribe failed, properties:{}", properties, e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    /**
     * 用来控制多个SmartLifecycle Bean的启动和停止顺序，一般规则是：
     * 启动时：phase小的先启动。
     * 停止时：phase大的先停止。
     * 这里面返回的是0，说明使用的是一个默认阶段，没有特殊的优先级。
     */
    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public void destroy() {
        this.stop();
    }
}
