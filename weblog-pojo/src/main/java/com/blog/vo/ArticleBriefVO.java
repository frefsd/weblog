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

    /**
     * 发布日期（MM-dd 格式，用于归档页展示）
     */
    private String createTime;

    /**
     * 所属分类名称（用于归档页展示分类标签）
     */
    private String categoryName;
}
