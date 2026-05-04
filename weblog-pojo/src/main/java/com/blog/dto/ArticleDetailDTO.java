package com.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticleDetailDTO {
    @NotNull(message = "articleId不能为空")
    private Long articleId;
}

