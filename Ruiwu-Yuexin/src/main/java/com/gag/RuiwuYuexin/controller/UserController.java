package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.dto.UserDTO;
import com.gag.RuiwuYuexin.entity.User;
import com.gag.RuiwuYuexin.service.UserService;
import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
    @PostMapping("/user/change-password")
    public Result changePassword(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        try {
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

            // 提取请求参数
            String oldPassword = requestBody.get("oldPassword");
            String newPassword = requestBody.get("newPassword");

            if (oldPassword == null || oldPassword.isEmpty()) {
                return Result.error("请提供原密码");
            }

            if (newPassword == null || newPassword.isEmpty()) {
                return Result.error("请提供新密码");
            }

            // 验证原密码是否正确
            if (!user.getPassword().equals(oldPassword)) {
                return Result.error("原密码错误");
            }

            // 验证新密码强度
            if (newPassword.length() < 6) {
                return Result.error("密码长度至少需要6个字符");
            }

            // 更新密码
            user.setPassword(newPassword);

            userService.updateUser(user);

            return Result.success("密码更新成功");
        } catch (DataAccessException e) {
            return Result.error("数据库错误: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("密码更新失败: " + e.getMessage());
        }
    }
    @PutMapping("/user/update")
    public Result updateUser(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ","");
        Long userId = jwtUtils.getUserIdFromToken(token);

        User user = userService.findById(userId);
        User existingUser = userService.findByUserName(userDTO.getUsername());
        if (existingUser != null && !userDTO.getUsername().equals(user.getUsername() )) {
            return Result.error("用户已存在");
        }
        BeanUtils.copyProperties(userDTO, user,"id");

        if (userDTO.getAvatar() != null && userDTO.getAvatar().contains(",")) {
            String base64Data = userDTO.getAvatar().split(",")[1];
            byte[] avatarBytes = Base64.getDecoder().decode(base64Data);
            user.setAvatarBytes(avatarBytes);
        }

        User updatedUser = userService.updateUser(user);
        return Result.success(updatedUser);
    }

}
