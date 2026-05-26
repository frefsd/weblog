package com.blog.controller;

import com.blog.dto.LogSearchDTO;
import com.blog.entity.AlertRecord;
import com.blog.entity.AlertRule;
import com.blog.entity.LogRecord;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.AlertService;
import com.blog.service.MonitorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监控管理控制器，提供日志查询、告警规则管理、告警记录查询接口。
 * 仅管理员角色可访问。
 */
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorLogService monitorLogService;
    private final AlertService alertService;

    @PostMapping("/log/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<LogRecord>> logPage(@RequestBody LogSearchDTO dto) {
        return Result.page(monitorLogService.page(dto));
    }

    @PostMapping("/rule/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<AlertRule>> ruleList() {
        return Result.ok(alertService.listRules());
    }

    @PostMapping("/rule/save")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> ruleSave(@RequestBody AlertRule rule) {
        alertService.saveRule(rule);
        return Result.ok();
    }

    @PostMapping("/rule/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> ruleDelete(@RequestParam("id") Long id) {
        alertService.deleteRule(id);
        return Result.ok();
    }

    @PostMapping("/alert/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<AlertRecord>> alertPage(@RequestParam(defaultValue = "1") Integer current,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        return Result.page(alertService.pageAlertRecord(current, size));
    }
}
