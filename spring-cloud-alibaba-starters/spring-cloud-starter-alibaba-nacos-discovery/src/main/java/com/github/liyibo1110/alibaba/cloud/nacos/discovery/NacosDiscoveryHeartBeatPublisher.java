package com.github.liyibo1110.alibaba.cloud.nacos.discovery;

import com.github.liyibo1110.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 类名有一些误导性，并不是给Nacos Server发送当前实例的保活心跳的，而是定期向Spring容器发布HeartbeatEvent，
 * 让Spring Cloud内的监听者知道“服务发现可能需要刷新”。
 * 属于Spring Cloud服务发现体系内部的“心跳事件发布器”，用来提醒框架里的其他监听者：服务发现的相关信息可能变化了，你们可以检查刷新一下。
 * @author liyibo
 * @date 2026-06-22 11:29
 */
public class NacosDiscoveryHeartBeatPublisher implements ApplicationEventPublisherAware, SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(NacosDiscoveryHeartBeatPublisher.class);

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    /** Spring提供的任务调度器，注意这个是自己new出来的，并不是通过Spring容器注入的，用来周期性执行publishHeartBeat */
    private final ThreadPoolTaskScheduler taskScheduler;

    /** 心跳事件的版本号，每发布一次事件，就递增一次，因为Spring Cloud有些组件监听HeartbeatEvent时，会检查value是否变化了 */
    private final AtomicLong nacosHeartBeatIndex = new AtomicLong(0);

    private final AtomicBoolean running = new AtomicBoolean(false);

    /** Spring的事件发布器，因此NacosDiscoveryHeartBeatPublisher是委托这个组件真正发事件 */
    private ApplicationEventPublisher publisher;

    /** 启动定时任务后的句柄，可以用来取消任务 */
    private ScheduledFuture<?> heartBeatFuture;

    public NacosDiscoveryHeartBeatPublisher(NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
        this.taskScheduler = getTaskScheduler();
    }

    private static ThreadPoolTaskScheduler getTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setBeanName("HeartBeat-Task-Scheduler");
        taskScheduler.initialize();
        return taskScheduler;
    }

    @Override
    public void start() {
        if (this.running.compareAndSet(false, true)) {
            log.info("Start nacos heartBeat task scheduler.");
            // 注意用的是fixedDelay，每30秒发一次Spring Cloud心跳事件
            this.heartBeatFuture = this.taskScheduler.scheduleWithFixedDelay(
                    this::publishHeartBeat, Duration.ofMillis(this.nacosDiscoveryProperties.getWatchDelay()));
        }
    }

    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false)) {
            if (this.heartBeatFuture != null) {
                // shutdown current user-thread,
                // then the other daemon-threads will terminate automatic.
                this.taskScheduler.shutdown();
                this.heartBeatFuture.cancel(true);
            }
        }
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    /**
     * 组件核心方法，发送心跳。
     */
    public void publishHeartBeat() {
        HeartbeatEvent event = new HeartbeatEvent(this, nacosHeartBeatIndex.getAndIncrement());
        this.publisher.publishEvent(event);
    }
}
