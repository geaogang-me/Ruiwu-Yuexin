package com.gag.RuiwuYuexin.entity;

import lombok.Data;
import java.time.LocalDateTime;
@Data
public class Shop {
    private Long id;
    private String shop_name;
    private Long user_id;
    private String telephone;
    private LocalDateTime created;
    private byte[] pic_shop;
    private Integer level;
}
