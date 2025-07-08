package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.dto.UserDTO;
import com.gag.RuiwuYuexin.entity.Shop;
import com.gag.RuiwuYuexin.entity.User;
import com.gag.RuiwuYuexin.service.ShopService;
import com.gag.RuiwuYuexin.service.UserService;
import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils; // 注入 JWT 工具类

    @Autowired
    private ShopService shopService;

    @PostMapping("/login")
    public Result<UserDTO> login(@RequestBody User userRequest) {
        User user = userService.findByUserName(userRequest.getUsername());
        if (user != null && user.getPassword().equals(userRequest.getPassword())) {
            // 登录成功后生成 Token
            String token = jwtUtils.generateToken(user.getId());
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
        return Result.error("用户名或密码不正确");
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        // 前端只需删除本地存储的 Token 即可，无需服务器端操作
        return Result.success("退出登录成功");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User registerRequest) {
        // 验证用户是否已存在
        User existingUser = userService.findByUserName(registerRequest.getUsername());
        if (existingUser != null) {
            return Result.error("用户已存在");
        }
        // 插入新用户
        userService.insertUser(registerRequest);
        return Result.success("用户注册成功");
    }
}
