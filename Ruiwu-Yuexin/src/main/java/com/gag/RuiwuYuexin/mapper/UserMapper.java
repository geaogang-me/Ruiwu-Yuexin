package com.gag.RuiwuYuexin.mapper;
import com.gag.RuiwuYuexin.entity.User;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface UserMapper  {
    public void insertUser(User user);

    User findByUserName(String username);
    User findById(Long userId);

    User findByUsernameAndPassword(String username, String password);
    int updateById( User user);

}
