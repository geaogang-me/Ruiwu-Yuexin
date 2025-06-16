package com.gag.RuiwuYuexin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_evaluate_images")
public class EvaluateImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 多对一关系：多张图片属于一个评价
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluate_id", nullable = false)
    private Evaluate evaluate;

    @Lob // 标记为大对象
    @Column(name = "image_data", nullable = false)
    private byte[] imageData; // 原始二进制数据

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "create_time", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}