package com.blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * swagger接口文档
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WeBlog API 文档")
                        .version("1.0.0")
                        .description("WeBlog 博客系统的后端接口文档，包含用户管理、文章发布、评论等功能。"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081") //后端端口
                                .description("本地开发服务器")
                ));
    }
}
