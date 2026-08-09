package com.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticleTopDTO {
    @NotNull(message = "articleId不能为空")
    private Long articleId;
}
