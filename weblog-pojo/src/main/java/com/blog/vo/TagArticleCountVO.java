package com.blog.vo;

import lombok.Data;

/**
 * 标签文章数量统计 VO（用于柱状图）
 */
@Data
public class TagArticleCountVO {

    /**
     * 标签 ID
     */
    private Long tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 文章数量
     */
    private Long articleCount;
}
