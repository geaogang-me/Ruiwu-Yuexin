package com.gag.RuiwuYuexin.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class User implements Serializable {
 private Long id;
 private String username;
 private String sex;
 private String password;
 private String phone;
 private String email;
 private String address;
 private String  avatar;
 private String role;
 @JsonFormat(pattern = "yyyy-MM-dd")
 private LocalDateTime createTime;
 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
 private LocalDateTime updateTime;  // 新增
 private static final long serialVersionUID = 1L;
}
