package com.slopeoasis.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.slopeoasis.user.interceptor.JwtInterceptor;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT interceptor validates tokens in dev mode (extracts claims without signature verification)
        // and in production mode (strict signature validation)
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/users/**")
                .excludePathPatterns("/users/public/**");
    }
}
