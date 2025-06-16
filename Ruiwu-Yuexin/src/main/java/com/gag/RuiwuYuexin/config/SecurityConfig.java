package com.gag.RuiwuYuexin.config;

import com.gag.RuiwuYuexin.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // ✅ 启用跨域支持
                   .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/verify-password","/user/change-password","/api/user/update","/api/user/info","/api/logout","/api/login",
                                "/api/register", "/api/good","/api/good/**","/api/addToCart","/api/list/{userId}","/api/cart/delete"
                                ,"/api/cart/count","/api/favorite/add","/api/favorite/remove","/api/favorite/check",
                                "/api/favorite/list","/api/address/list","/api/address/add","/api/address/update",
                                "/api/address/delete/{id}","/api/order/create","/api/order/list","/api/token/refresh",
                                "/api/review/add","/api/order/updateStatus","/api/order/batchUpdateStatus","/api/evaluations/{id}").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

