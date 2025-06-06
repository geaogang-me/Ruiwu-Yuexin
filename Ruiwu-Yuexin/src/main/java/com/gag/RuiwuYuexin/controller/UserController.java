package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.dto.UserDTO;
import com.gag.RuiwuYuexin.entity.User;
import com.gag.RuiwuYuexin.service.UserService;
import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;

    // 获取用户信息
    @GetMapping("/user/info")
    public Result getUserInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("缺少或格式错误的 Authorization 头");
        }

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        User user = userService.findById(userId);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return Result.success(user);
    }

    @PostMapping("/user/verify-password")
    public Result verifyPassword(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("缺少或格式错误的 Authorization 头");
        }

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);

        User user = userService.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 从请求体中提取 oldPassword
        String oldPassword = requestBody.get("oldPassword");
        if (oldPassword == null) {
            return Result.error("未提供原密码");
        }

        // 验证原密码是否正确
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }

        return Result.success("原密码正确");
    }
    @PutMapping("/user/update")
    public Result updateUser(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ","");
        Long userId = jwtUtils.getUserIdFromToken(token);

        User user = userService.findById(userId);
        BeanUtils.copyProperties(userDTO, user,"id");

        if (userDTO.getAvatar() != null && userDTO.getAvatar().contains(",")) {
            String base64Data = userDTO.getAvatar().split(",")[1];
            byte[] avatarBytes = Base64.getDecoder().decode(base64Data);
            user.setAvatarBytes(avatarBytes);
        }

        userService.updateUser(user);
        return Result.success("更新成功");
    }

}
