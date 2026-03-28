package com.blog.vo;

import lombok.Data;

import java.util.List;

/**
 * 前台文章详情 VO
 */
@Data
public class ArticleFrontendDetailVO {

    /**
     * 文章 ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 阅读量
     */
    private Integer readNum;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 标签列表
     */
    private List<TagSimpleVO> tags;

    /**
     * 上一篇文章
     */
    private ArticleBriefVO preArticle;

    /**
     * 下一篇文章
     */
    private ArticleBriefVO nextArticle;
}
