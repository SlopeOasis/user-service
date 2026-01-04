package com.slopeoasis.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow the frontend dev server to call the backend. Adjust origins for production.
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:3000",
                    "https://frontend-navy-iota-66.vercel.app",
                    "http://20.199.136.220:8080",
                    "https://20.199.136.220:8080"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .allowedHeaders("*");
    }
}
