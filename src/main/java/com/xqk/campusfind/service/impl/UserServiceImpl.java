package com.xqk.campusfind.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xqk.campusfind.entity.User;
import com.xqk.campusfind.mapper.UserMapper; // 🌟 引入我们建好的 Mapper
import com.xqk.campusfind.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 🌟 安全加密导包
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper; // 🌟 切换为 MyBatis-Plus 的 Mapper

    @Autowired
    private PasswordEncoder passwordEncoder; // 🌟 注入主类里定义好的加密器

    @Override
    public User login(String username, String password) {
        // 1. 根据用户名利用 MyBatis-Plus 优雅查询
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        // 2. 🌟 安全密文比对：matches(前端明文密码, 数据库存的加密密文)
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }
}