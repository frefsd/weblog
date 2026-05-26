package com.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志查询请求 DTO。
 */
@Data
public class LogSearchDTO {
    /** 日志级别筛选 */
    private String level;
    /** 请求类型筛选 */
    private String type;
    /** 当前页 */
    private Integer current = 1;
    /** 每页条数 */
    private Integer size = 10;
    /** 查询起始时间 */
    private LocalDateTime startTime;
    /** 查询截止时间 */
    private LocalDateTime endTime;
    /** 错误消息关键词搜索 */
    private String keyword;
}
