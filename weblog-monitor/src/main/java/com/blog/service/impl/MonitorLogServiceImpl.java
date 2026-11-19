package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.dto.LogSearchDTO;
import com.blog.entity.LogRecord;
import com.blog.mapper.MonitorLogMapper;
import com.blog.service.MonitorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 监控日志服务实现，负责日志写入和分页查询。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorLogServiceImpl extends ServiceImpl<MonitorLogMapper, LogRecord> implements MonitorLogService {

    /** 监控日志保留天数，超过该天数的记录由定时任务清理 */
    private static final int LOG_RETENTION_DAYS = 7;

    private final MonitorLogMapper monitorLogMapper;

    @Override
    public boolean save(LogRecord entity) {
        entity.setCreateTime(LocalDateTime.now());
        return monitorLogMapper.insert(entity) > 0;
    }

    @Override
    public void clearAll() {
        monitorLogMapper.delete(new LambdaQueryWrapper<>());
    }

    @Override
    public Page<LogRecord> page(LogSearchDTO dto) {
        Page<LogRecord> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<LogRecord> wrapper = new LambdaQueryWrapper<>();

        if (dto.getLevel() != null && !dto.getLevel().isBlank()) {
            wrapper.eq(LogRecord::getLevel, dto.getLevel());
        }
        if (dto.getType() != null && !dto.getType().isBlank()) {
            wrapper.eq(LogRecord::getType, dto.getType());
        }
        if (dto.getStartTime() != null) {
            wrapper.ge(LogRecord::getCreateTime, dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            wrapper.le(LogRecord::getCreateTime, dto.getEndTime());
        }
        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.like(LogRecord::getErrorMessage, dto.getKeyword());
        }

        wrapper.orderByDesc(LogRecord::getCreateTime);
        return monitorLogMapper.selectPage(page, wrapper);
    }

    /**
     * 每天凌晨 3:30 清理过期日志。
     *
     * <p>之前 monitor_log 只增不减（只有管理员手动点"清空"），
     * 配合分页插件缺失，日志表迟早把内存和磁盘一起吃满。</p>
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void purgeExpiredLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(LOG_RETENTION_DAYS);
        int removed = monitorLogMapper.delete(
                new LambdaQueryWrapper<LogRecord>().lt(LogRecord::getCreateTime, cutoff));
        log.info("清理过期监控日志: 保留 {} 天, 删除 {} 条", LOG_RETENTION_DAYS, removed);
    }
}
