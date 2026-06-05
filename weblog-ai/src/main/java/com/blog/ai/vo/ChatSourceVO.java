package com.blog.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 回答的引用来源 VO。
 *
 * <p>当 AI 的回答参考了博客文章时，将其作为来源列出，
 * 方便前端展示"参考文章"链接。</p>
 */
@Data
@AllArgsConstructor
public class ChatSourceVO {

    /** 文章 ID */
    private Long articleId;

    /** 文章标题 */
    private String articleTitle;

    /** 引用的文本片段（截取前 100 字符） */
    private String snippet;
}
