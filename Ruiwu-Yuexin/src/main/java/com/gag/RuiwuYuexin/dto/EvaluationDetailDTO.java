package com.gag.RuiwuYuexin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDetailDTO {
    // 评价基本信息
    private String userName;
    private String userAvatar;
    private Long id;
    private Long userId;
    private Long goodId;
    private Integer starLevel;
    private String comment;  // 与实体类保持一致
    private LocalDateTime createTime;

    // 评价图片列表
    private List<EvaluationImageDTO> images;
}