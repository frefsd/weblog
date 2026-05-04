package com.blog.vo;

import lombok.Data;

/**
 * 仪表盘文章统计 VO
 */
@Data
public class DashboardArticleStatisticsVO {

    /**
     * 文章总数（前端期望字段名）
     */
    private Long articleTotalCount;

    /**
     * 分类总数（前端期望字段名）
     */
    private Long categoryTotalCount;

    /**
     * 标签总数（前端期望字段名）
     */
    private Long tagTotalCount;

    /**
     * 总浏览量（前端期望字段名）
     */
    private Long pvTotalCount;

    /**
     * 文章总数（向后兼容）
     */
    private Long totalArticles;

    /**
     * 今日发布文章数
     */
    private Long todayArticles;

    /**
     * 昨日发布文章数
     */
    private Long yesterdayArticles;

    /**
     * 分类总数（向后兼容）
     */
    private Long totalCategories;

    /**
     * 标签总数（向后兼容）
     */
    private Long totalTags;
}
