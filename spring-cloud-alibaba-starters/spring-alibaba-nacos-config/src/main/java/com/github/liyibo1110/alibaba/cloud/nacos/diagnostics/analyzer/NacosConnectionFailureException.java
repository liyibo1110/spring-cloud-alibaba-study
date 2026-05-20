package com.github.liyibo1110.alibaba.cloud.nacos.diagnostics.analyzer;

/**
 * 连接到Nacos server失败对应的异常。
 * @author liyibo
 * @date 2026-05-20 23:41
 */
public class NacosConnectionFailureException extends RuntimeException {

    private final String serverAddr;

    public NacosConnectionFailureException(String serverAddr, String message) {
        super(message);
        this.serverAddr = serverAddr;
    }

    public NacosConnectionFailureException(String serverAddr, String message, Throwable cause) {
        super(message, cause);
        this.serverAddr = serverAddr;
    }

    public String getServerAddr() {
        return serverAddr;
    }
}
