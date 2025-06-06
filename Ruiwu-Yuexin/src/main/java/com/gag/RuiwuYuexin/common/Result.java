package com.gag.RuiwuYuexin.common;

import lombok.*;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result<T> implements Serializable {
    public static final String CODE_SUCCESS = "200";
    public static final String CODE_AUTH_ERROR = "401";
    public static final String CODE_SYS_ERROR = "500";

    private String code;         // 状态码
    private String msg;          // 提示信息
    private T data;              // 返回数据体
    private long timestamp;      // 返回时间戳（毫秒）

    // 成功 - 无数据
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(CODE_SUCCESS)
                .msg("请求成功")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // 成功 - 有数据
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(CODE_SUCCESS)
                .msg("请求成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // 失败 - 系统错误
    public static <T> Result<T> error() {
        return Result.<T>builder()
                .code(CODE_SYS_ERROR)
                .msg("系统错误")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // 失败 - 自定义提示
    public static <T> Result<T> error(String msg) {
        return Result.<T>builder()
                .code(CODE_SYS_ERROR)
                .msg(msg)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // 失败 - 自定义状态码 & 提示
    public static <T> Result<T> error(String code, String msg) {
        return Result.<T>builder()
                .code(code)
                .msg(msg)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
