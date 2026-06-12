package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.service.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * File upload controller backed by private CTYun ZOS object storage.
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private ObjectStorageService objectStorageService;

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
            e.printStackTrace();
            return Result.error("上传失败: " + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }
}
