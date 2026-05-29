package com.blog.vo;

import lombok.Data;

import java.util.List;

/**
 * 归档年份 VO（用于归档页年→月二级分组）
 */
@Data
public class ArchiveYearVO {

    /**
     * 年份（如 "2026"）
     */
    private String year;

    /**
     * 该年文章总数
     */
    private Integer articleCount;

    /**
     * 该年各月归档列表
     */
    private List<ArchiveMonthVO> months;
}
