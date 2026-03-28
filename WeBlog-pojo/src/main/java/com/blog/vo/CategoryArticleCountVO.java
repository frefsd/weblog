package com.blog.vo;

import lombok.Data;

/**
 * 分类文章数量统计 VO（用于扇形图）
 */
@Data
public class CategoryArticleCountVO {

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 文章数量
     */
    private Long articleCount;
}
