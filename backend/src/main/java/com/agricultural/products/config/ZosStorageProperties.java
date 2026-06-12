package com.agricultural.products.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "storage.zos")
public class ZosStorageProperties {
    private String endpoint;
    private String bucket;
    private String region;
    private String accessKey;
    private String secretKey;
    private Long presignExpireSeconds = 3600L;
    private Boolean pathStyleAccess = true;

    public boolean isConfigured() {
        return hasText(endpoint) && hasText(bucket) && hasText(region) && hasText(accessKey) && hasText(secretKey);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
