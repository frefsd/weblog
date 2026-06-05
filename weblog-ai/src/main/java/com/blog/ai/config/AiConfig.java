package com.blog.ai.config;

import com.blog.ai.service.InMemoryEmbeddingStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Slf4j
@Configuration
@EnableConfigurationProperties(AiProperties.class)
@MapperScan("com.blog.ai.mapper")
public class AiConfig {

    private static final String STORE_FILE = System.getProperty("user.dir") + "/data/embedding-store.json";

    @Bean
    public InMemoryEmbeddingStore embeddingStore(ObjectMapper objectMapper) {
        File file = new File(STORE_FILE);
        if (file.exists()) {
            try {
                return objectMapper.readValue(file, InMemoryEmbeddingStore.class);
            } catch (IOException e) {
                log.warn("向量索引文件损坏或无法读取，将创建空索引: {}", e.getMessage());
            }
        }
        return new InMemoryEmbeddingStore();
    }
}
