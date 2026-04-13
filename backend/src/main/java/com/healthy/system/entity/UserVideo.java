package com.healthy.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_video")
public class UserVideo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long actionId;
    private String videoUrl;
    private String thumbnailUrl;
    private String category;
    private Integer durationSec;
    private Long fileSize;
    private LocalDateTime createdTime;
}
