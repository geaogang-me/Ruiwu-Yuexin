package com.gag.RuiwuYuexin.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GoodsImage {
    private Integer id;
    private Integer goodId;
    private byte[] imageData;
    private Integer sortOrder;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}
