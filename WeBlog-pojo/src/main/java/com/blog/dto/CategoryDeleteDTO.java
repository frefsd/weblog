package com.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryDeleteDTO {
    @NotNull(message = "categoryId不能为空")
    private Long categoryId;
}

