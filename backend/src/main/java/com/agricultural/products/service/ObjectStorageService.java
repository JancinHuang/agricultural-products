package com.agricultural.products.service;

import com.agricultural.products.config.ZosStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ObjectStorageService {

    private static final String DEFAULT_DIRECTORY = "products";
    private static final Map<String, String> ALLOWED_EXTENSIONS = Map.of(
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png", "image/png",
            ".gif", "image/gif",
            ".webp", "image/webp"
    );
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.copyOf(ALLOWED_EXTENSIONS.values());

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ZosStorageProperties properties;

    public ObjectStorageService(S3Client s3Client, S3Presigner s3Presigner, ZosStorageProperties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    public UploadResult upload(MultipartFile file) throws IOException {
        ensureConfigured();
        validateImageFile(file);
        String objectKey = buildObjectKey(file.getOriginalFilename());
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .contentType(resolveContentType(file))
                .contentLength(file.getSize())
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return new UploadResult(objectKey, presign(objectKey), extractFilename(objectKey));
    }

    public String toAccessibleUrl(String image) {
        if (image == null || image.isBlank() || isAlreadyAccessible(image)) {
            return image;
        }
        try {
            return presign(image);
        } catch (IllegalStateException ignored) {
            return image;
        }
    }

    public String presign(String objectKey) {
        ensureConfigured();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.getPresignExpireSeconds()))
                .getObjectRequest(builder -> builder.bucket(properties.getBucket()).key(objectKey))
                .build();
        PresignedGetObjectRequest request = s3Presigner.presignGetObject(presignRequest);
        return request.url().toString();
    }

    private String buildObjectKey(String originalFilename) {
        String extension = extractExtension(originalFilename);
        String datePath = LocalDate.now().toString().replace("-", "/");
        return DEFAULT_DIRECTORY + "/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private String resolveContentType(MultipartFile file) {
        return file.getContentType() == null || file.getContentType().isBlank()
                ? "application/octet-stream"
                : file.getContentType();
    }

    private void validateImageFile(MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.containsKey(extension)) {
            throw new IllegalArgumentException("仅支持 jpg、jpeg、png、gif、webp 图片格式");
        }

        String contentType = resolveContentType(file).toLowerCase();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("文件类型不正确，请上传图片文件");
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }
        String filename = originalFilename.replace("\\", "/");
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase();
    }

    private String extractFilename(String objectKey) {
        int index = objectKey.lastIndexOf('/');
        return index >= 0 ? objectKey.substring(index + 1) : objectKey;
    }

    private boolean isAlreadyAccessible(String image) {
        return image.startsWith("http://") || image.startsWith("https://");
    }

    private void ensureConfigured() {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("ZOS object storage is not configured. Please set ZOS_ACCESS_KEY and ZOS_SECRET_KEY.");
        }
    }

    public record UploadResult(String objectKey, String url, String filename) {
    }
}
