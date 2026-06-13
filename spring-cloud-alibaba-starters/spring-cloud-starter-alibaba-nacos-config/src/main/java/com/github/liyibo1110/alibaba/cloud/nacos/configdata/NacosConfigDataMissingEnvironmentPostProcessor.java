package com.github.liyibo1110.alibaba.cloud.nacos.configdata;

import com.alibaba.cloud.nacos.NacosPropertiesPrefixer;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.cloud.commons.ConfigDataMissingEnvironmentPostProcessor;
import org.springframework.cloud.util.PropertyUtils;
import org.springframework.core.env.Environment;

import static com.alibaba.cloud.nacos.configdata.NacosConfigDataLocationResolver.PREFIX;

/**
 * 在使用2.4版本的ConfigData新机制，并且启用了Nacos Config的情况下，检查用户是否写了spring.config.import=nacos:这样的配置。
 * 如果没写，就让启动失败并且给出错误提示，相当于是一个启动前置的检查器。
 *
 * 父类ConfigDataMissingEnvironmentPostProcessor的作用检查spring.config.import是否忘了配置，因此子类是额外又检查了nacos:这个前缀。
 * @author liyibo
 * @date 2026-06-12 11:12
 */
public class NacosConfigDataMissingEnvironmentPostProcessor extends ConfigDataMissingEnvironmentPostProcessor {

    /** 保证这个PostProcessor会在父类后面执行 */
    public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 1000;

    @Override
    public int getOrder() {
        return ORDER;
    }

    /**
     * 子类要检查的特定前缀。
     * @return 就是nacos:
     */
    @Override
    protected String getPrefix() {
        return PREFIX;
    }

    /**
     * 核心方法：决定当前环境是否需要检查spring.config.import=nacos:
     */
    @Override
    protected boolean shouldProcessEnvironment(Environment environment) {
        // 1、如果启动了bootstrap旧机制，则取消检查
        // 2、如果启用了legacy processing，也取消检查
        if (PropertyUtils.bootstrapEnabled(environment) || PropertyUtils.useLegacyProcessing(environment))
            return false;
        // 3、计算Nacos配置的prefix，默认可能是spring.nacos，也可能通过NacosPropertiesPrefixer变成spring.cloud.nacos
        String prefix = NacosPropertiesPrefixer.getPrefix(environment);
        // 4、最终拼出configPrefix
        String configPrefix = prefix + ".config";

        // 5、读取{prefix}.config.enabled属性值（默认就是true）
        boolean configEnabled = environment.getProperty(configPrefix + ".enabled", Boolean.class, true);
        // 6、读取{prefix}.config.import-check.enabled（默认也是true）
        boolean importCheckEnabled = environment.getProperty(configPrefix + ".import-check.enabled", Boolean.class, true);
        // 7、以上两个全是true，才执行检查
        return configEnabled && importCheckEnabled;
    }

    /**
     * Spring Boot内部的失败分析器实现。
     * 当启动失败抛出ImportException时，给用户展示更友好的错误说明和解决建议。
     */
    static class ImportExceptionFailureAnalyzer extends AbstractFailureAnalyzer<ImportException> {
        @Override
        protected FailureAnalysis analyze(Throwable rootFailure, ImportException cause) {
            String description;
            if (cause.missingPrefix)
                description = "The spring.config.import property is missing a " + PREFIX + " entry";
            else
                description = "No spring.config.import property has been defined";

            String action = "Add a spring.config.import=nacos: property to your configuration.\n"
                    + "\tIf configuration is not required add spring.config.import=optional:nacos: instead.\n"
                    + "\tTo disable this check, set spring.cloud.nacos.config.import-check.enabled=false.";
            return new FailureAnalysis(description, action, cause);
        }

    }
}
