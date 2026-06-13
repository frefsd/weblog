package com.blog.vo;

import lombok.Data;

import java.util.List;

/**
 * 前台首页文章列表 VO
 */
@Data
public class ArticleIndexVO {

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
     * 文章描述
     */
    private String description;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 分类信息
     */
    private CategorySimpleVO category;

    /**
     * 标签列表
     */
    private List<TagSimpleVO> tags;

    /**
     * 阅读量
     */
    private Integer readNum;
}
