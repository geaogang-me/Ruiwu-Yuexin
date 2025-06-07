package com.gag.RuiwuYuexin.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Review {
    private Long id;
    private Long userId;
    private Integer goodId;
    private Integer rating;
    private String content;
    private List<String> images;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updateTime;
}
