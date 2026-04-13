package com.healthy.system.controller;

import com.healthy.system.common.ApiResponse;
import com.healthy.system.dto.CompareResultDto;
import com.healthy.system.dto.VideoUploadRequest;
import com.healthy.system.entity.KeypointAnnotation;
import com.healthy.system.entity.UserVideo;
import com.healthy.system.entity.VideoCompareRecord;
import com.healthy.system.mapper.KeypointAnnotationMapper;
import com.healthy.system.service.MinioService;
import com.healthy.system.service.VideoCompareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/video-compare/")
public class VideoCompareController {

    @Autowired
    private VideoCompareService videoCompareService;
    
    @Autowired
    private MinioService minioService;
    
    @Autowired
    private KeypointAnnotationMapper annotationMapper;

    @PostMapping("/upload")
    public ApiResponse<UserVideo> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("actionId") Long actionId,
            @RequestParam(value = "category", required = false) String category,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            String videoUrl = minioService.uploadTrainVideo(file);
            VideoUploadRequest request = new VideoUploadRequest();
            request.setActionId(actionId);
            request.setCategory(category);
            UserVideo video = videoCompareService.uploadVideo(userId, request, videoUrl);
            return ApiResponse.success(video);
        } catch (Exception e) {
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/videos")
    public ApiResponse<List<UserVideo>> getUserVideos(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "actionId", required = false) Long actionId) {
        List<UserVideo> videos = videoCompareService.getUserVideos(userId, actionId);
        return ApiResponse.success(videos);
    }

    @DeleteMapping("/videos/{videoId}")
    public ApiResponse<Void> deleteVideo(
            @PathVariable Long videoId,
            @RequestHeader("X-User-Id") Long userId) {
        videoCompareService.deleteVideo(userId, videoId);
        return ApiResponse.success();
    }

    @GetMapping("/actions")
    public ApiResponse<List> getTrainActions() {
        return ApiResponse.success(null);
    }

    @PostMapping("/compare")
    public ApiResponse<VideoCompareRecord> createCompare(
            @RequestBody VideoCompareRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        VideoCompareRecord record = videoCompareService.createCompare(
            userId, request.getUserVideoId(), request.getStdActionId());
        return ApiResponse.success(record);
    }

    @GetMapping("/compare/{recordId}")
    public ApiResponse<CompareResultDto> getCompareResult(@PathVariable Long recordId) {
        CompareResultDto result = videoCompareService.getCompareResult(recordId);
        return ApiResponse.success(result);
    }

    @GetMapping("/compare/history")
    public ApiResponse<List<VideoCompareRecord>> getCompareHistory(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "actionId", required = false) Long actionId) {
        List<VideoCompareRecord> history = videoCompareService.getCompareHistory(userId, actionId);
        return ApiResponse.success(history);
    }

    @GetMapping("/annotations/{actionId}")
    public ApiResponse<List<KeypointAnnotation>> getAnnotations(@PathVariable Long actionId) {
        List<KeypointAnnotation> annotations = annotationMapper.selectByActionId(actionId);
        return ApiResponse.success(annotations);
    }

    public static class VideoCompareRequest {
        private Long userVideoId;
        private Long stdActionId;
        public Long getUserVideoId() { return userVideoId; }
        public void setUserVideoId(Long userVideoId) { this.userVideoId = userVideoId; }
        public Long getStdActionId() { return stdActionId; }
        public void setStdActionId(Long stdActionId) { this.stdActionId = stdActionId; }
    }
}
