package com.gag.RuiwuYuexin.service;

import com.gag.RuiwuYuexin.entity.User;

import java.util.List;

public interface UserService {
    public void insertUser(User user);
    User findByUserName(String username);
    User findById(Long userId);
    User validateUser(String username, String password);
    void updateUser(User user);

}
