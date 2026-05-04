package com.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TagAddDTO {
    @NotEmpty(message = "tags不能为空")
    private List<String> tags;
}

