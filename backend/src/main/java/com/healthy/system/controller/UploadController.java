package com.healthy.system.controller;

import com.healthy.system.auth.UserContext;
import com.healthy.system.common.ApiResponse;
import com.healthy.system.entity.SysUser;
import com.healthy.system.entity.UserProfile;
import com.healthy.system.service.MinioService;
import com.healthy.system.service.SysUserService;
import com.healthy.system.service.UserProfileService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UploadController {

    private final MinioService minioService;
    private final SysUserService sysUserService;
    private final UserProfileService userProfileService;

    public UploadController(MinioService minioService,
                            SysUserService sysUserService,
                            UserProfileService userProfileService) {
        this.minioService = minioService;
        this.sysUserService = sysUserService;
        this.userProfileService = userProfileService;
    }

    @PostMapping("/avatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(userId);
        if (account == null) {
            return ApiResponse.error(401, "用户不存在");
        }
        if (account.getProfileId() == null) {
            return ApiResponse.error(400, "请先完善个人信息后再上传头像");
        }

        String avatarUrl;
        try {
            avatarUrl = minioService.uploadAvatar(file, userId);
        } catch (IllegalArgumentException e) {
            // 将具体原因（如大小超限）透传给前端
            return ApiResponse.error(400, e.getMessage());
        }

        if (account.getProfileId() != null) {
            UserProfile profile = userProfileService.getById(account.getProfileId());
            if (profile != null) {
                profile.setAvatarUrl(avatarUrl);
                userProfileService.updateById(profile);
            }
        }

        return ApiResponse.success(avatarUrl);
    }

    /**
     * 上传训练动作图片，返回图片 URL。
     */
    @PostMapping("/train/image")
    public ApiResponse<String> uploadTrainImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = minioService.uploadTrainImage(file);
            return ApiResponse.success(url);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 上传训练动作视频，返回视频 URL。
     */
    @PostMapping("/train/video")
    public ApiResponse<String> uploadTrainVideo(@RequestParam("file") MultipartFile file) {
        try {
            String url = minioService.uploadTrainVideo(file);
            return ApiResponse.success(url);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
