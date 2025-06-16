package com.gag.RuiwuYuexin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationImageDTO {
    private Long id;
    private Integer sortOrder;
    private String imageData; // Base64编码的图片
}
