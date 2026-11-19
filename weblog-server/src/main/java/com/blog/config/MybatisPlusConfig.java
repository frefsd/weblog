package com.blog.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置。
 *
 * <p>分页插件必须显式注册，否则 {@code selectPage} 不会生成 LIMIT，
 * 会把整表结果集加载进内存（monitor_log 这类持续增长的表会直接 OOM）。</p>
 */
@Configuration
public class MybatisPlusConfig {

    /** 单页最大条数，防止前端传入超大 size 打爆内存 */
    private static final long MAX_PAGE_SIZE = 200L;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(MAX_PAGE_SIZE);
        // 页码越界时返回空列表，而不是重定向到首页（避免前端无限翻页）
        pagination.setOverflow(false);
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}
