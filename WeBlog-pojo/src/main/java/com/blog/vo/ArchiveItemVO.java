package com.blog.vo;

import lombok.Data;

import java.util.List;

/**
 * 归档项 VO
 */
@Data
public class ArchiveItemVO {

    /**
     * 年月（格式：yyyy-MM）
     */
    private String month;

    /**
     * 文章列表
     */
    private List<ArticleBriefVO> articles;
}
