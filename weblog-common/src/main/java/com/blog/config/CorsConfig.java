package com.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS解决前后端跨域问题
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有来源 (开发环境方便，生产环境建议指定具体域名)
        configuration.setAllowedOriginPatterns(List.of("*"));
        //允许的请求方法
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        //允许的请求头
        configuration.setAllowedHeaders(List.of("*"));
        // 暴露 Authorization 头给前端 ，让前端拿到Token
        configuration.setExposedHeaders(List.of("Authorization"));
        // 允许携带凭证 (Cookie/Token)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
