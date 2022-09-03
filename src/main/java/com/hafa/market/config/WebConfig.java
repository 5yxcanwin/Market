package com.hafa.market.config;

import com.hafa.market.interceptor.CheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author heavytiger
 * @version 1.0
 * @description Web应用配置类
 * @date 2022/4/17 13:11
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CheckInterceptor checkInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 项目中的所有接口都支持跨域
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("*")
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加鉴权拦截器，拦截所有请求，排除登录相关的API调用
        registry.addInterceptor(checkInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/article/getImage/*", "/temp/*");
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
