package com.xqk.campusfind.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 🌟 动态注入 yml 中的配置项
    @Value("${file.web-path}")
    private String webPath;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 🛡️ 告别硬编码！路径完全由配置文件决定
        registry.addResourceHandler(webPath)
                .addResourceLocations("file:" + uploadDir);
    }
}