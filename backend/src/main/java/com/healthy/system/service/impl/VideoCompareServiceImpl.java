package com.healthy.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthy.system.dto.CompareResultDto;
import com.healthy.system.dto.VideoUploadRequest;
import com.healthy.system.entity.KeypointAnnotation;
import com.healthy.system.entity.TrainAction;
import com.healthy.system.entity.UserVideo;
import com.healthy.system.entity.VideoCompareRecord;
import com.healthy.system.mapper.KeypointAnnotationMapper;
import com.healthy.system.mapper.TrainActionMapper;
import com.healthy.system.mapper.UserVideoMapper;
import com.healthy.system.mapper.VideoCompareRecordMapper;
import com.healthy.system.service.VideoCompareService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class VideoCompareServiceImpl implements VideoCompareService {

    @Autowired
    private UserVideoMapper userVideoMapper;
    
    @Autowired
    private VideoCompareRecordMapper compareRecordMapper;
    
    @Autowired
    private KeypointAnnotationMapper annotationMapper;
    
    @Autowired
    private TrainActionMapper trainActionMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public UserVideo uploadVideo(Long userId, VideoUploadRequest request, String videoUrl) {
        UserVideo video = new UserVideo();
        video.setUserId(userId);
        video.setActionId(request.getActionId());
        video.setVideoUrl(videoUrl);
        video.setCategory(request.getCategory());
        video.setCreatedTime(LocalDateTime.now());
        userVideoMapper.insert(video);
        return video;
    }

    @Override
    public List<UserVideo> getUserVideos(Long userId, Long actionId) {
        LambdaQueryWrapper<UserVideo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVideo::getUserId, userId);
        if (actionId != null) {
            wrapper.eq(UserVideo::getActionId, actionId);
        }
        wrapper.orderByDesc(UserVideo::getCreatedTime);
        return userVideoMapper.selectList(wrapper);
    }

    @Override
    public void deleteVideo(Long userId, Long videoId) {
        LambdaQueryWrapper<UserVideo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVideo::getId, videoId).eq(UserVideo::getUserId, userId);
        userVideoMapper.delete(wrapper);
    }

    @Override
    public VideoCompareRecord createCompare(Long userId, Long userVideoId, Long stdActionId) {
        VideoCompareRecord record = new VideoCompareRecord();
        record.setUserId(userId);
        record.setUserVideoId(userVideoId);
        record.setStdActionId(stdActionId);
        record.setStatus("PENDING");
        record.setCreatedTime(LocalDateTime.now());
        compareRecordMapper.insert(record);
        return record;
    }

    @Override
    public CompareResultDto getCompareResult(Long recordId) {
        VideoCompareRecord record = compareRecordMapper.selectById(recordId);
        if (record == null) return null;
        
        CompareResultDto dto = new CompareResultDto();
        dto.setRecordId(record.getId());
        dto.setOverallScore(record.getOverallScore());
        dto.setStatus(record.getStatus());
        
        if (record.getCompareResult() != null) {
            try {
                CompareResultDto parsed = objectMapper.readValue(record.getCompareResult(), CompareResultDto.class);
                dto.setKeypoints(parsed.getKeypoints());
            } catch (Exception e) {
                dto.setKeypoints(new ArrayList<>());
            }
        }
        return dto;
    }

    @Override
    public List<VideoCompareRecord> getCompareHistory(Long userId, Long actionId) {
        LambdaQueryWrapper<VideoCompareRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoCompareRecord::getUserId, userId);
        if (actionId != null) {
            wrapper.eq(VideoCompareRecord::getStdActionId, actionId);
        }
        wrapper.orderByDesc(VideoCompareRecord::getCreatedTime);
        return compareRecordMapper.selectList(wrapper);
    }
    
    public List<KeypointAnnotation> getAnnotationsByActionId(Long actionId) {
        return annotationMapper.selectByActionId(actionId);
    }
}
