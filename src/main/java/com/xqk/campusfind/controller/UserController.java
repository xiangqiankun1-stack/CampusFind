package com.xqk.campusfind.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xqk.campusfind.common.Result;
import com.xqk.campusfind.entity.User;
import com.xqk.campusfind.mapper.UserMapper;
import com.xqk.campusfind.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid; // 确保引入

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${secure.admin-key:campusfind@admin123}")
    private String secureAdminKey;

    /**
     * 用户登录（修复第4个漏洞：敏感信息脱敏）
     */
    @PostMapping("/login")
    public Result<User> login(@RequestParam String username, @RequestParam String password) {
        User user = userService.login(username, password);
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        // 🔒 安全脱敏：返回给前端前，主动将密码哈希抹除，防止密码特征和哈希暴露
        user.setPassword(null);

        return Result.success(user);
    }

    /**
     * 用户注册（修复第5个漏洞：启用 @Valid 强校验拦截）
     */
    @PostMapping("/register")
    public Result<User> register(
            @Valid @RequestBody User user, // 🌟 激活校验
            @RequestParam(value = "inputKey", required = false) String inputKey
    ) {
        // 管理员秘钥核验
        if (user.getRole() != null && user.getRole() == 1) {
            if (inputKey == null || !secureAdminKey.equals(inputKey.trim())) {
                return Result.error("管理员注册失败：授权密钥错误或已失效！");
            }
        }

        // 检查用户名重复
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())
        );
        if (count > 0) {
            return Result.error("该用户名已被注册，请更换！");
        }

        // 数据强加密后落库
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setRole(user.getRole());

        userMapper.insert(newUser);

        // 注册返回对象也顺手脱敏
        newUser.setPassword(null);
        return Result.success(newUser);
    }
}