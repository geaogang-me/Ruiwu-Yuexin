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
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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
        String uri = request.getRequestURI();
        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                // 解析 JWT，获取用户 ID
                Long userId = jwtUtils.getUserIdFromToken(jwt);

                // 从 Redis 校验 token 是否有效
                String redisKey = "TOKEN_" + userId;
                String redisToken = redisTemplate.opsForValue().get(redisKey);
                if (redisToken == null || !redisToken.equals(jwt)) {
                    // Token 无效，若是 logout 请求则放行
                    if ("/api/logout".equals(uri)) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    ResponseUtils.unauthorized(response, "Token 已失效或已登出，请重新登录");
                    return;
                }

                // 设置 Spring Security 上下文
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException e) {
            logger.warn("Token 已过期：{}", e);
            // Token 过期，返回特定错误码，前端统一处理刷新或重新登录
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"code\":\"TOKEN_EXPIRED\",\"msg\":\"Token 已过期，请刷新或重新登录\"}"
            );
            return;
        } catch (JwtException e) {
            logger.error("无法解析 Token：", e);
            // Token 异常（例如过期），若是 logout 请求则放行
            if ("/api/logout".equals(uri)) {
                filterChain.doFilter(request, response);
                return;
            }
            ResponseUtils.unauthorized(response, "Token 无效：" + e.getMessage());
            return;
        }

        // 继续执行其它过滤器或请求处理
        filterChain.doFilter(request, response);
    }
}
