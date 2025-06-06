package com.gag.RuiwuYuexin.entity;
import lombok.Data;
import java.io.Serializable;

@Data
public class Cart implements Serializable {
    private Long id;
    private Long userId;
    private Long goodId;
    private Integer num;
    private static final long serialVersionUID = 1L;
}