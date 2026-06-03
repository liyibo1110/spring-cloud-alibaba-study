package com.github.liyibo1110.alibaba.cloud.nacos.parser;

import org.springframework.core.io.ByteArrayResource;

/**
 * @author liyibo
 * @date 2026-06-02 17:04
 */
public class NacosByteArrayResource extends ByteArrayResource {

    private String filename;

    public NacosByteArrayResource(byte[] byteArray) {
        super(byteArray);
    }

    public NacosByteArrayResource(byte[] byteArray, String description) {
        super(byteArray, description);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return this.filename == null ? this.getDescription() : this.filename;
    }
}
