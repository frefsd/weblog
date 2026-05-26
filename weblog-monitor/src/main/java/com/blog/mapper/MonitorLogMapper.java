package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.LogRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MonitorLogMapper extends BaseMapper<LogRecord> {
}
