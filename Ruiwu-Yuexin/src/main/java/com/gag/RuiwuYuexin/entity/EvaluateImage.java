package com.gag.RuiwuYuexin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EvaluateImage {
    private Long id;
    private Long evaluateId;
    private byte[] imageData;
    private Integer sortOrder;
    private LocalDateTime createTime;
}