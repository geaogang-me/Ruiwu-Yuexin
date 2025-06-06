package com.gag.RuiwuYuexin.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")      // 从配置文件中读取密钥
    private String secret;

    @Value("${jwt.expiration}") // Token 有效期（毫秒）
    private long expiration;

    // 生成 Token
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析 Token 中的用户名，并允许一定时间偏差（例如60秒）
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(60)
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    public long getExpiration() {
        return expiration;
    }


    // 生成签名密钥
    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
