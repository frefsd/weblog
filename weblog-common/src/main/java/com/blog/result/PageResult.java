package com.blog.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页统一返回结构
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {
    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码（前端字段：current，从 1 开始）
     */
    private Long current;

    /**
     * 每页大小（前端字段：size）
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size, long pages) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(records);
        r.setTotal(total);
        r.setCurrent(current);
        r.setSize(size);
        r.setPages(pages);
        return r;
    }
}

