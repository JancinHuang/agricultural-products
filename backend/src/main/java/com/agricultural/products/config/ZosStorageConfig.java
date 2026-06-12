package com.agricultural.products.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(ZosStorageProperties.class)
public class ZosStorageConfig {

    @Bean
    public S3Client s3Client(ZosStorageProperties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(s3Configuration(properties))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(ZosStorageProperties properties) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(s3Configuration(properties))
                .build();
    }

    private StaticCredentialsProvider credentialsProvider(ZosStorageProperties properties) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(nullToEmpty(properties.getAccessKey()), nullToEmpty(properties.getSecretKey()))
        );
    }

    private S3Configuration s3Configuration(ZosStorageProperties properties) {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(Boolean.TRUE.equals(properties.getPathStyleAccess()))
                .build();
    }

    private String nullToEmpty(String value) {
        return value == null || value.isBlank() ? "missing" : value;
    }
}
