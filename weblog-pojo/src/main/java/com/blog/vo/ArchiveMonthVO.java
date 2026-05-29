package com.blog.vo;

import lombok.Data;

import java.util.List;

/**
 * 归档月份 VO（用于归档页年→月二级分组）
 */
@Data
public class ArchiveMonthVO {

    /**
     * 月份（如 "05月"）
     */
    private String month;

    /**
     * 该月文章数量
     */
    private Integer articleCount;

    /**
     * 该月文章列表
     */
    private List<ArticleBriefVO> articles;
}
