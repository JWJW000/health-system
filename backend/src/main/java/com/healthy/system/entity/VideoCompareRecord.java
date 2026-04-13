package com.healthy.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("video_compare_record")
public class VideoCompareRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long userVideoId;
    private Long stdActionId;
    private String compareResult;
    private Integer overallScore;
    private String status;
    private LocalDateTime createdTime;
}
