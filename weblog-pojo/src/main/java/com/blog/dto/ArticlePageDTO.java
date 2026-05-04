package com.blog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticlePageDTO {

    /**
     * 当前页码（前端字段：current，从 1 开始）
     */
    @NotNull(message = "current不能为空")
    @Min(value = 1, message = "current必须>=1")
    private Long current;

    /**
     * 每页大小（前端字段：size）
     */
    @NotNull(message = "size不能为空")
    @Min(value = 1, message = "size必须>=1")
    @Max(value = 200, message = "size不能超过200")
    private Long size;

    /**
     * 前端 moment 格式：YYYY-MM-DD HH:mm:ss
     */
    private String startDate;

    private String endDate;

    /**
     * 文章标题模糊搜索
     */
    private String searchTitle;
}

