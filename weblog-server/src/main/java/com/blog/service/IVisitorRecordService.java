package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.VisitorRecord;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>
 * 访客记录表 服务类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
public interface IVisitorRecordService extends IService<VisitorRecord> {

    /**
     * 异步记录访客信息（IP、User-Agent、访问时间）
     */
    void recordVisitorAsync(HttpServletRequest request);
}
