package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryAddDTO {
    @NotBlank(message = "name不能为空")
    private String name;
}

