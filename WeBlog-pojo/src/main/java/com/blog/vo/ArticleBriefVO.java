package com.blog.vo;

import lombok.Data;

/**
 * 文章简要信息 VO（用于上下篇、归档）
 */
@Data
public class ArticleBriefVO {

    /**
     * 文章 ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章封面图
     */
    private String titleImage;
}
