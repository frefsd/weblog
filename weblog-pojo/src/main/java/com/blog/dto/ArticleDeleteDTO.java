package com.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticleDeleteDTO {
    @NotNull(message = "articleId不能为空")
    private Long articleId;
}

