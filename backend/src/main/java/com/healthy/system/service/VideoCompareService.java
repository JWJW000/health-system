package com.healthy.system.service;

import com.healthy.system.dto.CompareResultDto;
import com.healthy.system.dto.VideoUploadRequest;
import com.healthy.system.entity.UserVideo;
import com.healthy.system.entity.VideoCompareRecord;
import java.util.List;

public interface VideoCompareService {
    UserVideo uploadVideo(Long userId, VideoUploadRequest request, String videoUrl);
    List<UserVideo> getUserVideos(Long userId, Long actionId);
    void deleteVideo(Long userId, Long videoId);
    VideoCompareRecord createCompare(Long userId, Long userVideoId, Long stdActionId);
    CompareResultDto getCompareResult(Long recordId);
    List<VideoCompareRecord> getCompareHistory(Long userId, Long actionId);
}
