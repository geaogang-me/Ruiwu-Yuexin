package com.gag.RuiwuYuexin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_evaluates")
public class Evaluate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "good_id", nullable = false)
    private Long goodId;

    @Column(name = "star_level", nullable = false)
    private Integer starLevel;

    @Column(name = "evaluate", columnDefinition = "VARCHAR(255)")
    private String comment; // 使用comment作为字段名更符合实际含义

    @Column(name = "create_time", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // 一对多关系：一个评价对应多张图片
    @OneToMany(mappedBy = "evaluate", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<EvaluateImage> images = new ArrayList<>();

    // 添加图片的方法
    public void addImage(EvaluateImage image) {
        images.add(image);
        image.setEvaluate(this);
    }

    // 移除图片的方法
    public void removeImage(EvaluateImage image) {
        images.remove(image);
        image.setEvaluate(null);
    }
}