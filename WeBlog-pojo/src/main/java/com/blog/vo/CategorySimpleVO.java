package com.blog.vo;

import lombok.Data;

/**
 * 分类简单信息 VO
 */
@Data
public class CategorySimpleVO {

    /**
     * 分类 ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;
}
