package com.gag.RuiwuYuexin.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gag.RuiwuYuexin.common.Result;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class ResponseUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeJson(HttpServletResponse response, int status, Result result) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    public static void unauthorized(HttpServletResponse response, String message) throws IOException {
        writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, Result.error(Result.CODE_AUTH_ERROR, message));
    }

    public static void forbidden(HttpServletResponse response, String message) throws IOException {
        writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error("403", message));
    }

    public static void notFound(HttpServletResponse response, String message) throws IOException {
        writeJson(response, HttpServletResponse.SC_NOT_FOUND, Result.error("404", message));
    }

    public static void internalServerError(HttpServletResponse response, String message) throws IOException {
        writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Result.error(Result.CODE_SYS_ERROR, message));
    }
    public static void ok(HttpServletResponse resp, Object data) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(
                MAPPER.writeValueAsString(Map.of("success", true, "data", data))
        );
    }
}
