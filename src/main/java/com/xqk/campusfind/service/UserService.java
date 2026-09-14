package com.xqk.campusfind.service;
import com.xqk.campusfind.entity.User;

public interface UserService {
    User login(String username, String password);
}