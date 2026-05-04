package com.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TagDeleteDTO {
    @NotNull(message = "tagId不能为空")
    private Long tagId;
}

