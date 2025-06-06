package com.gag.RuiwuYuexin.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
    private final String code;

    public ServiceException(String code, String message) {
        super(message);
        this.code = code;
    }
}