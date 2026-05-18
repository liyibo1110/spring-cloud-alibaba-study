package com.github.liyibo1110.alibaba.cloud.nacos;

/**
 * SPI扩展点接口，用来获取配置前缀
 * @author liyibo
 * @date 2026-05-18 16:13
 */
public interface NacosPropertiesPrefixProvider {

    String getPrefix();
}
