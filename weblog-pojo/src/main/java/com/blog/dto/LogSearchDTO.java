package com.blog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(value = 1, message = "current必须>=1")
    private Integer current = 1;
    /** 每页条数（限制上限，避免一次拉取整张日志表） */
    @Min(value = 1, message = "size必须>=1")
    @Max(value = 200, message = "size不能超过200")
    private Integer size = 10;
    /** 查询起始时间 */
    private LocalDateTime startTime;
    /** 查询截止时间 */
    private LocalDateTime endTime;
    /** 错误消息关键词搜索 */
    private String keyword;
}
