package com.gag.RuiwuYuexin.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDTO implements Serializable {
    private Long id;
    private String username;
    private String avatar;
    private String role;
    private Long shopId;
    private String token;
    private Long expire;
}
