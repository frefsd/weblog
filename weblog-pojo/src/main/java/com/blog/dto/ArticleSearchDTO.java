package com.blog.dto;

import lombok.Data;

@Data
public class ArticleSearchDTO {
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 当前页码（从1开始）
     */
    private Long current = 1L;
    
    /**
     * 每页大小
     */
    private Long size = 10L;
}