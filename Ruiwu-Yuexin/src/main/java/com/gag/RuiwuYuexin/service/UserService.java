package com.gag.RuiwuYuexin.service;

import com.gag.RuiwuYuexin.entity.User;

public interface UserService {
    void insertUser(User user);
    User findByUserName(String username);
    User findById(Long userId);
    User validateUser(String username, String password);
    User updateUser(User user);

}
