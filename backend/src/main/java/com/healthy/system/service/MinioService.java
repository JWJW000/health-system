package com.healthy.system.service;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * MinIO 对象存储服务，用于头像、健身图、训练动作媒体等文件上传。
 */
public interface MinioService {

    /**
     * 上传用户头像，返回可访问的 URL。
     *
     * @param file   头像文件
     * @param userId 系统用户 ID（用于路径/对象名）
     * @return 对象访问 URL
     */
    String uploadAvatar(MultipartFile file, Long userId);

    /**
     * 上传打卡健身图片，返回可访问的 URL。
     *
     * @param file   图片文件
     * @param userId 用户档案 ID（user_profile.id）
     * @param date   打卡日期
     * @return 对象访问 URL
     */
    String uploadCheckInImage(MultipartFile file, Long userId, LocalDate date);

    /**
     * 上传训练动作配图，返回可访问的 URL。
     *
     * @param file 图片文件
     * @return 对象访问 URL
     */
    String uploadTrainImage(MultipartFile file);

    /**
     * 上传训练动作演示视频，返回可访问的 URL。
     *
     * @param file 视频文件
     * @return 对象访问 URL
     */
    String uploadTrainVideo(MultipartFile file);
}
