package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.entity.User;
import com.gag.RuiwuYuexin.mapper.UserMapper;
import com.gag.RuiwuYuexin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class UserServiceImpl  implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public void insertUser(User user) {
        userMapper.insertUser(user);
    }

    @Override
    public User findByUserName(String username) {
        return userMapper.findByUserName(username);
    }

    @Override
    public User findById(Long userId) {
        return userMapper.findById(userId);
    }

    @Override
    public User validateUser(String username, String password) {
        return userMapper.findByUsernameAndPassword(username, password);
    }
    @Override
    public User updateUser(User user) {
        userMapper.updateById(user);
        return userMapper.findById(user.getId());
    }

}
