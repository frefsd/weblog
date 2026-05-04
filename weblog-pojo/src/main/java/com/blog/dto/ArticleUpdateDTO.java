package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ArticleUpdateDTO {
    @NotNull(message = "id不能为空")
    private Long id;

    @NotBlank(message = "title不能为空")
    private String title;

    @NotBlank(message = "content不能为空")
    private String content;

    @NotBlank(message = "titleImage不能为空")
    private String titleImage;

    @NotBlank(message = "description不能为空")
    private String description;

    @NotNull(message = "categoryId不能为空")
    private Long categoryId;

    @NotNull(message = "tags不能为空")
    private List<Object> tags;
}

