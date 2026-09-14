package com.xqk.campusfind.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xqk.campusfind.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 基础的增删改查和按用户名查询，MyBatis-Plus 已经内置或可以通过 Wrapper 搞定
}