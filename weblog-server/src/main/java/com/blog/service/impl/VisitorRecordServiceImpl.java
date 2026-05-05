package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.VisitorRecord;
import com.blog.mapper.VisitorRecordMapper;
import com.blog.service.IVisitorRecordService;
import com.blog.utils.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 访客记录表 服务实现类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Slf4j
@Service
public class VisitorRecordServiceImpl extends ServiceImpl<VisitorRecordMapper, VisitorRecord> implements IVisitorRecordService {

    @Async("visitorTaskExecutor")
    @Override
    public void recordVisitorAsync(HttpServletRequest request) {
        try {
            String ip = IpUtil.getClientIp(request);
            String ua = request.getHeader("User-Agent");

            VisitorRecord record = new VisitorRecord();
            record.setIpAddress(ip);
            record.setVisitor(ua != null && ua.length() > 20 ? ua.substring(0, 20) : ua);
            record.setVisitTime(LocalDateTime.now());
            // ip_region 使用表字段默认值 "未知"
            // is_notify 使用表字段默认值 0
            this.save(record);
        } catch (Exception e) {
            log.error("记录访客信息失败", e);
        }
    }
}
