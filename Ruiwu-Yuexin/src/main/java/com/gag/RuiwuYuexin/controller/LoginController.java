package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.dto.UserDTO;
import com.gag.RuiwuYuexin.entity.Shop;
import com.gag.RuiwuYuexin.entity.User;
import com.gag.RuiwuYuexin.service.ShopService;
import com.gag.RuiwuYuexin.service.UserService;
import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils; // 注入 JWT 工具类

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ShopService shopService;

    @PostMapping("/login")
    public Result<UserDTO> login(@RequestBody User userRequest) {
        User user = userService.findByUserName(userRequest.getUsername());
        if (user != null && user.getPassword().equals(userRequest.getPassword())) {
            // 登录成功后生成 Token
            String token = jwtUtils.generateToken(user.getId());
            // 存储 Token 到 Redis，并设置过期时间（例如1小时）
            redisTemplate.opsForValue().set("TOKEN_" + user.getId(), token, 1, TimeUnit.HOURS);

            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            userDTO.setToken(token);
            userDTO.setExpire(jwtUtils.getExpiration());

            Shop shop = shopService.findByUserId(user.getId());
            if (shop != null) {
                userDTO.setShopId(shop.getId());
            }
            return Result.success(userDTO);
        }
        return Result.error("用户名密码不正确");
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        // 从请求头获取Token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error("无效的Token格式");
        }

        // 提取真正的Token值
        String jwtToken = token.substring(7);

        try {
            // 验证并解析Token
            Long userId = jwtUtils.getUserIdFromToken(jwtToken);

            // 删除Redis中的Token
            String redisKey = "TOKEN_" + userId;
            Boolean deleted = redisTemplate.delete(redisKey);

            if (deleted != null && deleted) {
                return Result.success("退出登录成功");
            }
            return Result.error("Token不存在或已过期");
        } catch (ExpiredJwtException e) {
            // Token已过期的情况处理
            String username = e.getClaims().getSubject();
            redisTemplate.delete("TOKEN_" + username);
            return Result.error("登录已过期，请重新登录");
        } catch (Exception e) {
            return Result.error("退出登录失败：" + e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User registerRequest) {
        // 验证用户是否已存在
        User existingUser = userService.findByUserName(registerRequest.getUsername());
        if (existingUser != null) {
            return Result.error("用户已存在");
        }
        if (registerRequest.getAvatar() != null && registerRequest.getAvatar().contains(",")) {
            String base64Data = registerRequest.getAvatar().split(",")[1];
            byte[] avatarBytes = Base64.getDecoder().decode(base64Data);
            registerRequest.setAvatarBytes(avatarBytes);
        }
        // 插入新用户
        userService.insertUser(registerRequest);
        return Result.success("User registered successfully");
    }
}
