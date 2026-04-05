package com.blog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class WeBlogServerApplicationTests {

    /**
     * 生成 BCrypt 加密密码
     * 运行此测试后，在控制台查看输出的加密密码字符串
     *
     */
    @Test
    void generateBcryptPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String username = "admin";
        String rawPassword = "libaizuishuai@admin";

        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("===========================================");
        System.out.println("用户名: " + username);
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("===========================================");
    }
}
