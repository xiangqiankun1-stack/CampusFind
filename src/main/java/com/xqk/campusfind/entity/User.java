package com.xqk.campusfind.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "注册账号不能为空")
    @Size(min = 3, max = 15, message = "账号长度需在 3 到 15 个字符之间")
    private String username;

    @NotBlank(message = "登录密码不能为空")
    @Size(min = 5, message = "为了账户安全，密码长度不能少于 5 位")
    private String password;

    @NotNull(message = "账户身份角色类型不能为空")
    private Integer role;
}