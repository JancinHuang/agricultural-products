package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.service.ObjectStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final ObjectStorageService objectStorageService;

    public UploadController(ObjectStorageService objectStorageService) {
        this.objectStorageService = objectStorageService;
    }

    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        try {
            ObjectStorageService.UploadResult uploadResult = objectStorageService.upload(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", uploadResult.url());
            data.put("objectKey", uploadResult.objectKey());
            data.put("filename", uploadResult.filename());
            return Result.success("上传成功", data);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
