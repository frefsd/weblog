package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.LogSearchDTO;
import com.blog.entity.LogRecord;

/**
 * 监控日志服务接口，提供日志持久化和分页查询。
 */
public interface MonitorLogService extends IService<LogRecord> {
    Page<LogRecord> page(LogSearchDTO dto);
}
