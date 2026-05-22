package com.github.liyibo1110.alibaba.cloud.nacos.configdata;

/**
 * 配置偏好设置。
 * 当配置了特定于配置文件的配置时，本地配置将覆盖远程配置，因为本地配置是特定于配置文件的，因此具有更高的优先级。
 *
 * 因此，为了让远程配置有机会“胜出”，我们将远程配置视为特定于配置文件的配置，应将其置于特定于配置文件的同级导入之后。
 * 最终，它将覆盖本地特定于配置文件的配置。
 * @author liyibo
 * @date 2026-05-22 10:21
 */
public enum ConfigPreference {
    LOCAL,
    REMOTE
}
