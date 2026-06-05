package com.blog.ai.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArticleChangeEvent {
    private Long articleId;
    private String title;
    private String content;
    private ChangeType type;

    public enum ChangeType {
        CREATED, UPDATED, DELETED
    }
}
