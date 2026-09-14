package com.xqk.campusfind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CampusFindApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusFindApplication.class, args);
    }

    // 🌟 将 BCrypt 加密器注入到 Spring 容器中，方便在 Service 层随时 @Resource 注入使用
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}