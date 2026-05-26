package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警记录，对应 monitor_alert_record 表，记录每次告警触发的结果。
 */
@Data
@TableName("monitor_alert_record")
public class AlertRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的告警规则 ID */
    private Long ruleId;

    /** 触发时的日志级别 */
    private String logLevel;

    /** 实际触发的日志数量 */
    private Integer triggerCount;

    /** 阈值 */
    private Integer threshold;

    /** 通知状态：1-成功，0-失败 */
    private Integer notifyStatus;

    /** 触发时的错误消息 */
    private String errorMessage;

    /** 创建时间 */
    private LocalDateTime createTime;
}
