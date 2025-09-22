package com.gag.RuiwuYuexin.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : 葛澳港
 * @description :
 * @createDate : 2025/9/22 08:25
 */
@Data
public class UserInfoDto implements Serializable {
        private Long id;
        private String username;
        private String sex;
        private String phone;
        private String email;
        private String address;
        private String avatar;
        private String role;
        private Long shopId;
        private String token;
        private Long expire;
}
