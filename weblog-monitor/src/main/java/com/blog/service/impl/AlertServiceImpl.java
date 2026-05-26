package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.AlertRecord;
import com.blog.entity.AlertRule;
import com.blog.mapper.AlertRecordMapper;
import com.blog.mapper.AlertRuleMapper;
import com.blog.mapper.MonitorLogMapper;
import com.blog.service.AlertService;
import com.blog.utils.MailNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警服务实现，负责告警规则的增删查、告警触发评估（阈值比较 + 防重复 + 邮件通知）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl extends ServiceImpl<AlertRuleMapper, AlertRule> implements AlertService {

    private final AlertRuleMapper alertRuleMapper;
    private final MonitorLogMapper monitorLogMapper;
    private final AlertRecordMapper alertRecordMapper;
    private final MailNotifier mailNotifier;

    @Override
    public List<AlertRule> listEnabledRules() {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRule::getStatus, 1);
        return alertRuleMapper.selectList(wrapper);
    }

    @Override
    public void saveRule(AlertRule rule) {
        LocalDateTime now = LocalDateTime.now();
        if (rule.getId() == null) {
            rule.setCreateTime(now);
            rule.setUpdateTime(now);
            alertRuleMapper.insert(rule);
        } else {
            rule.setUpdateTime(now);
            alertRuleMapper.updateById(rule);
        }
    }

    @Override
    public void deleteRule(Long id) {
        alertRuleMapper.deleteById(id);
    }

    @Override
    public List<AlertRule> listRules() {
        return alertRuleMapper.selectList(null);
    }

    @Override
    public void evaluateAndNotify(AlertRule rule, String logLevel) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(rule.getTimeWindow());

        Long count = monitorLogMapper.selectCount(
                new LambdaQueryWrapper<com.blog.entity.LogRecord>()
                        .eq(com.blog.entity.LogRecord::getLevel, logLevel)
                        .ge(com.blog.entity.LogRecord::getCreateTime, since)
        );

        if (count < rule.getThreshold()) {
            return;
        }

        // 防重复：同一规则在时间窗口内已通知过则跳过
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(rule.getTimeWindow());
        Long recentNotify = alertRecordMapper.selectCount(
                new LambdaQueryWrapper<AlertRecord>()
                        .eq(AlertRecord::getRuleId, rule.getId())
                        .ge(AlertRecord::getCreateTime, windowStart)
        );
        if (recentNotify > 0) {
            log.debug("规则 {} 在时间窗口内已通知过，跳过", rule.getName());
            return;
        }

        // 获取最近一条错误消息
        com.blog.entity.LogRecord latestError = monitorLogMapper.selectOne(
                new LambdaQueryWrapper<com.blog.entity.LogRecord>()
                        .eq(com.blog.entity.LogRecord::getLevel, logLevel)
                        .orderByDesc(com.blog.entity.LogRecord::getCreateTime)
                        .last("LIMIT 1")
        );
        String errorMsg = latestError != null ? latestError.getErrorMessage() : null;

        // 发送邮件通知
        boolean notified = mailNotifier.sendAlert(
                rule.getName(), logLevel, count.intValue(), rule.getThreshold(), errorMsg
        );

        // 记录告警记录
        AlertRecord record = new AlertRecord();
        record.setRuleId(rule.getId());
        record.setLogLevel(logLevel);
        record.setTriggerCount(count.intValue());
        record.setThreshold(rule.getThreshold());
        record.setNotifyStatus(notified ? 1 : 0);
        record.setErrorMessage(errorMsg);
        record.setCreateTime(LocalDateTime.now());
        alertRecordMapper.insert(record);
    }

    @Override
    public Page<AlertRecord> pageAlertRecord(Integer current, Integer size) {
        Page<AlertRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AlertRecord::getCreateTime);
        return alertRecordMapper.selectPage(page, wrapper);
    }
}
