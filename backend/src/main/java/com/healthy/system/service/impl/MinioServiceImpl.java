package com.healthy.system.service.impl;

import com.healthy.system.config.MinioProperties;
import com.healthy.system.service.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;


    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024;
    private static final long MAX_CHECKIN_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_TRAIN_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_TRAIN_VIDEO_SIZE = 500L * 1024 * 1024;
    private static final String IMAGE_CONTENT_TYPE_PREFIX = "image/";
    private static final String VIDEO_CONTENT_TYPE_PREFIX = "video/";

    public MinioServiceImpl(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("头像大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw new IllegalArgumentException("请上传图片文件");
        }

        String bucket = properties.getBucket();
        ensureBucketExists(bucket);

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String objectName = "avatars/" + userId + "/" + UUID.randomUUID() + ext;

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("上传失败: " + e.getMessage(), e);
        }

        return properties.getEndpoint() + "/" + bucket + "/" + objectName;
    }

    @Override
    public String uploadCheckInImage(MultipartFile file, Long userId, LocalDate date) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_CHECKIN_IMAGE_SIZE) {
            throw new IllegalArgumentException("健身图片大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw new IllegalArgumentException("请上传图片文件");
        }

        String bucket = properties.getBucket();
        ensureBucketExists(bucket);

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String objectName = "checkin/" + userId + "/" + dateStr + "/" + UUID.randomUUID() + ext;

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("上传失败: " + e.getMessage(), e);
        }

        return properties.getEndpoint() + "/" + bucket + "/" + objectName;
    }

    @Override
    public String uploadTrainImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_TRAIN_IMAGE_SIZE) {
            throw new IllegalArgumentException("动作图片大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw new IllegalArgumentException("请上传图片文件");
        }

        String bucket = properties.getBucket();
        ensureBucketExists(bucket);

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String objectName = "train/actions/images/" + UUID.randomUUID() + ext;

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("上传失败: " + e.getMessage(), e);
        }

        return properties.getEndpoint() + "/" + bucket + "/" + objectName;
    }

    @Override
    public String uploadTrainVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_TRAIN_VIDEO_SIZE) {
            throw new IllegalArgumentException("动作视频大小不能超过 50MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(VIDEO_CONTENT_TYPE_PREFIX)) {
            throw new IllegalArgumentException("请上传视频文件");
        }

        String bucket = properties.getBucket();
        ensureBucketExists(bucket);

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".mp4";
        String objectName = "train/actions/videos/" + UUID.randomUUID() + ext;

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("上传失败: " + e.getMessage(), e);
        }

        return properties.getEndpoint() + "/" + bucket + "/" + objectName;
    }

    private void ensureBucketExists(String bucket) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("检查/创建桶失败: " + e.getMessage(), e);
        }
    }
}
