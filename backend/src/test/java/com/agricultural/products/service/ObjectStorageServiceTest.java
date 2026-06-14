package com.agricultural.products.service;

import com.agricultural.products.config.ZosStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ObjectStorageServiceTest {

    private S3Client s3Client;
    private ObjectStorageService objectStorageService;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        S3Presigner s3Presigner = mock(S3Presigner.class);

        ZosStorageProperties properties = new ZosStorageProperties();
        properties.setEndpoint("https://example.com");
        properties.setBucket("bucket");
        properties.setRegion("region");
        properties.setAccessKey("access-key");
        properties.setSecretKey("secret-key");

        objectStorageService = new ObjectStorageService(s3Client, s3Presigner, properties);
    }

    @Test
    void uploadRejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "shell.jsp",
                "image/jpeg",
                "fake".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> objectStorageService.upload(file));
        verifyNoInteractions(s3Client);
    }

    @Test
    void uploadRejectsUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "text/plain",
                "fake".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> objectStorageService.upload(file));
        verifyNoInteractions(s3Client);
    }
}
