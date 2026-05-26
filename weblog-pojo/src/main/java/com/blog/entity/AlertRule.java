package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警规则，对应 monitor_alert_rule 表，定义在指定时间窗口内某级别日志超过阈值时触发告警。
 */
@Data
@TableName("monitor_alert_rule")
public class AlertRule {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则名称 */
    private String name;

    /** 监控的日志级别 */
    private String logLevel;

    /** 时间窗口（分钟） */
    private Integer timeWindow;

    /** 触发阈值（时间窗口内日志条数） */
    private Integer threshold;

    /** 启用状态：1-启用，0-禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
