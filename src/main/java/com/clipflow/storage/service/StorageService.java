package com.clipflow.storage.service;

import com.clipflow.common.exception.BusinessException;
import com.clipflow.storage.config.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class StorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024;

    public StorageService(
            MinioClient minioClient,
            MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    public String uploadVideo(MultipartFile file, Long userId) {

        if (file == null || file.isEmpty()) {
            throw new BusinessException(20001, "视频文件不能为空");
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new BusinessException(20002, "视频文件不能超过100MB");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.startsWith("video/")) {
            throw new BusinessException(20003, "只允许上传视频文件");
        }

        String objectName =
                "videos/" + userId + "/" + UUID.randomUUID() + ".mp4";

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(contentType)
                            .build()
            );

            return objectName;
        } catch (Exception exception) {
            throw new BusinessException(20004, "视频上传失败");
        }
    }

    public void delete(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .build()
            );
        } catch (Exception exception) {
            throw new BusinessException(20005, "文件删除失败");
        }
    }

    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception exception) {
            throw new BusinessException(20006, "生成视频访问地址失败");
        }
    }
}