package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.dto.LogSearchDTO;
import com.blog.entity.LogRecord;
import com.blog.mapper.MonitorLogMapper;
import com.blog.service.MonitorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 监控日志服务实现，负责日志写入和分页查询。
 */
@Service
@RequiredArgsConstructor
public class MonitorLogServiceImpl extends ServiceImpl<MonitorLogMapper, LogRecord> implements MonitorLogService {

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
}
