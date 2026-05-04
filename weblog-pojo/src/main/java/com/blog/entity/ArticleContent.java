package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 文章内容表
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("article_content")
public class ArticleContent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文章内容id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 正文内容
     */
    private String content;


}
