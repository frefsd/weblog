package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 监控日志记录，对应 monitor_log 表，由 AOP 切面采集后写入。
 */
@Data
@TableName("monitor_log")
public class LogRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 服务名称 */
    private String serviceName;

    /** 日志级别：INFO / WARN / ERROR */
    private String level;

    /** 请求类型：HTTP_REQUEST / EXTERNAL_API */
    private String type;

    /** 方法全限定名 */
    private String method;

    /** 请求 URI */
    private String uri;

    /** HTTP 方法：GET / POST / PUT / DELETE */
    private String httpMethod;

    /** 请求耗时（毫秒） */
    private Long duration;

    /** 错误信息 */
    private String errorMessage;

    /** 异常堆栈（最多 20 行） */
    private String stackTrace;

    /** 客户端 IP */
    private String ip;

    /** 创建时间 */
    private LocalDateTime createTime;
}
