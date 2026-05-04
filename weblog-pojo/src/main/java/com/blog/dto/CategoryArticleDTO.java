package com.blog.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类文章查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryArticleDTO extends ArticlePageDTO {

    /**
     * 分类 ID
     */
    private Long categoryId;
}
