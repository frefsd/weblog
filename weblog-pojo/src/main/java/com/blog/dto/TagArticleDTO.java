package com.blog.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签文章查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TagArticleDTO extends ArticlePageDTO {

    /**
     * 标签 ID
     */
    private Long tagId;
}
