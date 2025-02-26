package com.xue.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${video.upload:/data/uploadVideo}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /home/file/**为前端URL访问路径 后面 file:xxxx为本地磁盘映射
        registry.addResourceHandler("/file/**").addResourceLocations("file:" + "/data/");
        registry.addResourceHandler("/**").addResourceLocations("file:" + "/");
        registry.addResourceHandler("/data1/**").addResourceLocations("file:" + "/data1/");
        registry.addResourceHandler("/data/certificate/**").addResourceLocations("file:" + "/data/certificate/");
    }
}
