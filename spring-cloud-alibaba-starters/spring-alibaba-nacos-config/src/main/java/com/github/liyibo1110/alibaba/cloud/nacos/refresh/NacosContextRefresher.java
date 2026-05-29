package com.github.liyibo1110.alibaba.cloud.nacos.refresh;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigManager;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosConfigProperties;
import com.github.liyibo1110.alibaba.cloud.nacos.NacosPropertySourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 在应用启动完成后，遍历之前已经记载并登记过的NacosPropertySource（已存到了Repostiory里），给每个可刷新的dataId/group注册Nacos Listener。
 * 之后当Nacos配置变更时，Listener将收到回调，会再发布一个Spring事件NacosConfigRefreshEvent。
 * @author liyibo
 * @date 2026-05-27 22:29
 */
public class NacosContextRefresher implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

    private final static Logger log = LoggerFactory.getLogger(NacosContextRefresher.class);

    /** 当前JVM进程内，Nacos Config刷新事件，累计发生的次数 */
    private static final AtomicLong REFRESH_COUNT = new AtomicLong(0);

    private final boolean isRefreshEnabled;

    /** 用于记录刷新的历史，给actuator endpoint查看用 */
    private final NacosRefreshHistory nacosRefreshHistory;

    private NacosConfigProperties nacosConfigProperties;

    /** nacos api组件，在这里用来注册监听器 */
    private ConfigService configService;

    private NacosConfigManager configManager;

    /** Spring容器引用，用来发布Spring事件 */
    private ApplicationContext applicationContext;

    private AtomicBoolean ready = new AtomicBoolean(false);

    /** listener缓存，key是dataId + group */
    private Map<String, Listener> listenerMap = new ConcurrentHashMap<>(16);

    public NacosContextRefresher(NacosConfigManager nacosConfigManager, NacosRefreshHistory refreshHistory) {
        this.configManager = nacosConfigManager;
        this.nacosConfigProperties = nacosConfigManager.getNacosConfigProperties();
        this.nacosRefreshHistory = refreshHistory;
        this.isRefreshEnabled = this.nacosConfigProperties.isRefreshEnabled();
    }

    public static long getRefreshCount() {
        return REFRESH_COUNT.get();
    }

    public static void refreshCountIncrement() {
        REFRESH_COUNT.incrementAndGet();
    }

    /**
     * 当应用程序启动后自动调用。
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 保证只运行一次，因为此方法可能会被Spring多次调用
        if (this.ready.compareAndSet(false, true))
            registerNacosListenersForApplications();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 注册
     */
    private void registerNacosListenersForApplications() {
        // 全局是否关闭了refresh
        if(isRefreshEnabled()) {
            for(var propertySource : NacosPropertySourceRepository.getAll()) {
                if(!propertySource.isRefreshable())
                    continue;
                String dataId = propertySource.getDataId();
                registerNacosListener(propertySource.getGroup(), dataId);
            }
        }
    }

    /**
     * 给特定dataId + group注册监听器。
     */
    private void registerNacosListener(final String groupKey, final String dataKey) {
        String key = NacosPropertySourceRepository.getMapKey(dataKey, groupKey);
        // 已经有了就直接返回，没有则创建并放入
        Listener listener = listenerMap.computeIfAbsent(key,
                lst -> new AbstractSharedListener() {
                    @Override
                    public void innerReceive(String dataId, String group, String configInfo) {
                        // 1、打印日志，说明哪个dataId / group的配置发生变化了
                        log.info("[Nacos Config] Receive Nacos config change: dataId={}, group={}", dataKey, groupKey);
                        // 2、refresh计数器加1
                        refreshCountIncrement();
                        // 3、记录刷新历史
                        nacosRefreshHistory.addRefreshRecord(dataId, group, configInfo);
                        // 4、把最新的配置内容放到快照管理器中，为了后续刷新过程或诊断过程，能拿到最新收到的配置内容
                        NacosSnapshotConfigManager.putConfigSnapshot(dataId, group, configInfo);
                        // 5、封装并发布Spring事件，说明这里并不负责真正的刷新，后面还有别的组件监听NacosConfigRefreshEvent，然后触发Environment更新、RefreshScope刷新或相关逻辑
                        NacosConfigRefreshEvent event = new NacosConfigRefreshEvent(this, null, "Refresh Nacos config");
                        event.setDataId(dataId);
                        event.setGroup(group);
                        applicationContext.publishEvent(event);
                        if (log.isDebugEnabled())
                            log.debug(String.format("Publish Nacos config Refresh Event group=%s,dataId=%s,configInfo=%s", group, dataId, configInfo));
                    }
                });
        try {
            // 懒加载ConfigService
            if (configService == null && configManager != null)
                configService = configManager.getConfigService();
            // 将listener真正注册到Nacos Client上，当Nacos Server上对应dataId/group发生变化时，这个listener的innerReceive就会被回调
            configService.addListener(dataKey, groupKey, listener);
            log.info("[Nacos Config] Listening config: dataId={}, group={}", dataKey, groupKey);
        } catch (NacosException e) {
            log.warn(String.format("register fail for nacos listener ,dataId=[%s],group=[%s]", dataKey, groupKey), e);
        }
    }

    public NacosConfigProperties getNacosConfigProperties() {
        return nacosConfigProperties;
    }

    public NacosContextRefresher setNacosConfigProperties(
            NacosConfigProperties nacosConfigProperties) {
        this.nacosConfigProperties = nacosConfigProperties;
        return this;
    }

    /**
     * 全局refresh是否开启，如果全局关闭了刷新功能，则不注册任何listener。
     */
    public boolean isRefreshEnabled() {
        if(nacosConfigProperties == null)
            return isRefreshEnabled;
        if(nacosConfigProperties.isRefreshEnabled() && !isRefreshEnabled)
            return false;
        return isRefreshEnabled;
    }
}
