package com.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDetailVO {
    private Long id;
    private String title;
    private String titleImage;
    private String description;

    private String content;
    private Long categoryId;
    private List<Long> tagIds;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

