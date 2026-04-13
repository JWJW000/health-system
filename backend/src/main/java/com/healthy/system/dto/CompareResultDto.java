package com.healthy.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class CompareResultDto {
    private Long recordId;
    private Integer overallScore;
    private String status;
    private List<KeypointResult> keypoints;
    
    @Data
    public static class KeypointResult {
        private String keypointType;
        private String description;
        private Integer detectedAngle;
        private Integer standardAngle;
        private String status;
        private String correctRange;
    }
}
