package com.gag.RuiwuYuexin.entity;
import lombok.Data;

@Data
public class Address {
    private Long id;
    private int userId;
    private String receiver;
    private String telephone;
    private String city;
    private String address;
}