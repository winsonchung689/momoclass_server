package com.xue.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

public class config extends WebMvcConfigurerAdapter {
    @Value("${xiaow.video.upload}")
    private String uploadUrl;


    @Value("${xiaow.video.mapping}")
    private String mappingUrl;


    /**
     * 这里配置一下虚拟映射，即我们访问file/**，但实际访问的资源是E:/file/video/**，从而实现对本地文件的访问
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler(mappingUrl+"/**").addResourceLocations("file:"+ uploadUrl + "/");
        super.addResourceHandlers(registry);
    }



}
