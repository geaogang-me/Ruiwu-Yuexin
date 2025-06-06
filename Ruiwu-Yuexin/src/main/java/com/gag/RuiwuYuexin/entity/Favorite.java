package com.gag.RuiwuYuexin.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Favorite {
    private Long id;
    private Long userId;
    private Long goodId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createTime;
}
