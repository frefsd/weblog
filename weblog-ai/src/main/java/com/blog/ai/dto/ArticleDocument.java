package com.blog.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArticleDocument {
    private Long id;
    private String title;
    private String content;
}
