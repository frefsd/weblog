package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.AlertRecord;
import com.blog.entity.AlertRule;

import java.util.List;

/**
 * 告警服务接口，管理告警规则的增删查和告警触发评估。
 */
public interface AlertService extends IService<AlertRule> {
    List<AlertRule> listEnabledRules();
    void saveRule(AlertRule rule);
    void deleteRule(Long id);
    List<AlertRule> listRules();
    void evaluateAndNotify(AlertRule rule);
    Page<AlertRecord> pageAlertRecord(Integer current, Integer size);
}
