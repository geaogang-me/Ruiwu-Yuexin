package com.gag.RuiwuYuexin.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Evaluate {
    private Long id;
    private Long userId;
    private Long goodId;
    private Integer starLevel;
    private String comment;
    private LocalDateTime createTime;
    private List<EvaluateImage> images = new ArrayList<>();

    public void addImage(EvaluateImage image) {
        this.images.add(image);
        image.setEvaluateId(this.id);
    }

    public void removeImage(EvaluateImage image) {
        this.images.remove(image);
        image.setEvaluateId(null);
    }
}