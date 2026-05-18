package com.github.liyibo1110.alibaba.cloud.nacos;

import com.github.liyibo1110.alibaba.cloud.nacos.utils.StringUtils;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.ServiceLoader;

/**
 * 决定某些nacos相关配置，应该从哪个配置前缀下面读取。
 * 即应该读spring.nacos.xxx呢？还是读某个SPI扩展指定的前缀？或者用户显式指定的前缀？
 * 如果没有任何额外的配置，默认从spring.nacos.xxx开始获取
 * @author liyibo
 * @date 2026-05-18 15:06
 */
public final class NacosPropertiesPrefixer {

    /** SPI提供的prefix */
    public static final String PREFIX = getPrefixFromSpi();

    private static String getPrefixFromSpi() {
        ServiceLoader<NacosPropertiesPrefixProvider> load = ServiceLoader.load(NacosPropertiesPrefixProvider.class);
        for (NacosPropertiesPrefixProvider provider : load)
            return provider.getPrefix();

        return "";
    }

    /**
     * 从Spring Environment读取配置。
     */
    public static String getPrefix(Environment environment) {
        String prefix = "spring.nacos"; // 默认返回值
        // 先从Environment里取
        String prefixFromProperties = environment.getProperty("spring.nacos.properties.prefix");
        if (StringUtils.isBlank(prefixFromProperties)) {
            if (StringUtils.isNotBlank(NacosPropertiesPrefixer.PREFIX))
                prefix = NacosPropertiesPrefixer.PREFIX;
        } else {
            prefix = prefixFromProperties;  // 找到了则用这个
        }

        if (StringUtils.isNotBlank(prefix) && prefix.endsWith("."))
            prefix = prefix.substring(0, prefix.length() - 1);

        return prefix;
    }

    /**
     * 和上面的功能一致
     */
    public static String getPrefix(Binder binder) {
        String prefix = "spring.nacos";
        BindResult<String> bind = binder.bind("spring.nacos.properties.prefix", String.class);
        if (!bind.isBound()) {
            if (StringUtils.isNotBlank(NacosPropertiesPrefixer.PREFIX))
                prefix = NacosPropertiesPrefixer.PREFIX;
        } else {
            prefix = bind.get();
        }

        if (StringUtils.isNotBlank(prefix) && prefix.endsWith("."))
            prefix = prefix.substring(0, prefix.length() - 1);

        return prefix;
    }
}
