package com.blog.controller;


import com.blog.dto.AlertPageDTO;
import com.blog.dto.LogSearchDTO;
import com.blog.entity.AlertRecord;
import com.blog.entity.AlertRule;
import com.blog.entity.LogRecord;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.AlertService;
import com.blog.service.MonitorLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 监控管理控制器，提供日志查询、告警规则管理、告警记录查询接口。
 * 仅管理员角色可访问。
 */
@Tag(name = "监控中心相关接口", description = "用于日志监控、告警规则和告警记录的管理")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    //依赖注入
    private final MonitorLogService monitorLogService;
    private final AlertService alertService;

    /**
     * 分页查询日志
     *
     * @param dto
     * @return
     */
    @Operation(summary = "分页查询日志")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/log/page")
    public Result<PageResult<LogRecord>> logPage(@RequestBody LogSearchDTO dto) {
        return Result.page(monitorLogService.page(dto));
    }

    /**
     * 清空所有日志记录（不可恢复）
     *
     * @return
     */
    @Operation(summary = "清空所有日志")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/log/clear")
    public Result<Void> clearLogs() {
        monitorLogService.clearAll();
        return Result.ok();
    }

    /**
     * 获取所有告警规则列表
     *
     * @return
     */
    @Operation(summary = "获取告警规则列表")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rule/list")
    public Result<List<AlertRule>> ruleList() {
        return Result.ok(alertService.listRules());
    }

    /**
     * 新增或更新告警规则
     *
     * @param rule
     * @return
     */
    @Operation(summary = "新增或更新告警规则")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rule/save")
    public Result<Void> ruleSave(@RequestBody AlertRule rule) {
        alertService.saveRule(rule);
        return Result.ok();
    }

    /**
     * 根据id删除告警规则
     *
     * @param id
     * @return
     */
    @Operation(summary = "删除告警规则")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rule/delete")
    public Result<Void> ruleDelete(@RequestParam("id") Long id) {
        alertService.deleteRule(id);
        return Result.ok();
    }

    /**
     * 分页查询告警触发记录
     *
     * @param dto
     * @return
     */
    @Operation(summary = "分页查询告警记录")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/alert/page")
    public Result<PageResult<AlertRecord>> alertPage(@Valid @RequestBody AlertPageDTO dto) {
        return Result.page(alertService.pageAlertRecord(dto.getCurrent().intValue(), dto.getSize().intValue()));
    }
}
