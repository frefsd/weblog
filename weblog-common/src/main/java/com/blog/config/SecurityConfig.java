package com.blog.config;

import com.blog.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor // Lombok 会自动生成包含 final 字段的构造函数
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 先处理 CORS，确保 OPTIONS 请求不被拦截
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 2. 关闭 CSRF
                .csrf(csrf -> csrf.disable())
                // 3. 禁用默认 LogoutFilter，由自定义 /logout 控制器处理退出
                .logout(logout -> logout.disable())
                // 4. 无状态会话
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. 放行 Swagger 所有静态资源和核心接口
                        .requestMatchers(
                                "/swagger-ui/**",              // Swagger UI 所有资源
                                "/swagger-ui.html",            // 显式放行 HTML 入口 (以防 MvcMatcher 不识别 /**)
                                "/v3/api-docs/**",             // OpenAPI JSON 数据
                                "/v3/api-docs.yaml",           // 有时前端请求的是 yaml
                                "/swagger-resources/**",       // 资源列表
                                "/webjars/**",                 // 前端依赖库 (js/css)
                                "/swagger-initializer.js",     // Swagger 初始化脚本
                                "/login"                       //放行登录接口
                        ).permitAll()
                        // 2. 放行前台接口（前台展示页面不需要登录）
                        .requestMatchers(
                                "/index/**",                   // 前台首页接口
                                "/article/**",                 // 前台文章接口
                                "/category/**",                // 前台分类接口
                                "/tag/**",                     // 前台标签接口
                                "/archive/**",                 // 前台归档接口
                                "/blog/**",                    // 博客设置接口
                                "/game/**"                     // 前台游戏接口
                        ).permitAll()
                        // 明确放行 GET 和 POST 的 /login 请求,做兜底
                        .requestMatchers(HttpMethod.GET, "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        // 除了上面放行的，其他所有的请求都需要认证
                        .anyRequest().authenticated()

                )

                // 异常处理：401 未认证 / 403 权限不足，统一返回 JSON（前端据此区分跳登录还是弹提示）
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"success\":false,\"message\":\"请先登录\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"success\":false,\"message\":\"权限不足：您没有执行该操作的权限\"}");
                        })
                )

                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        ;

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}