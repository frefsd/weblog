package com.blog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TagPageDTO {
    @NotNull(message = "current不能为空")
    @Min(value = 1, message = "current必须>=1")
    private Long current;

    @NotNull(message = "size不能为空")
    @Min(value = 1, message = "size必须>=1")
    @Max(value = 200, message = "size不能超过200")
    private Long size;

    private String startDate;
    private String endDate;

    /**
     * 前端字段：tagName（模糊查询）
     */
    private String tagName;
}

