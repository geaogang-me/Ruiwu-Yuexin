package com.gag.RuiwuYuexin.filter;

import com.gag.RuiwuYuexin.utils.JwtUtils;
import com.gag.RuiwuYuexin.utils.ResponseUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/login")
                || path.startsWith("/api/register")
                || path.startsWith("/api/good")
                || path.startsWith("/api/token/refresh");
    }

    // 从请求头中提取 Token
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = parseJwt(request);
        if (jwt != null) {
            try {
                // 1. 验签并检查是否过期（JwtUtils 内部会抛 ExpiredJwtException）
                Long userId = jwtUtils.getUserIdFromToken(jwt);

                // 2. 直接在这里判断有没有过期（可选，因为上一步如果过期会抛异常）
                if (jwtUtils.isTokenExpired(jwt)) {
                    throw new ExpiredJwtException(null, null, "Token 已过期");
                }

                // 3. 构造 Spring Security Context
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId, null, Collections.emptyList());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e) {
                // 过期异常 → 返回 401 + TOKEN_EXPIRED
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"code\":\"TOKEN_EXPIRED\",\"msg\":\"Token 已过期，请重新登录\"}");
                return;
            } catch (JwtException e) {
                // 签名异常或其他解析错误
                ResponseUtils.unauthorized(response, "Token 无效：" + e.getMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
