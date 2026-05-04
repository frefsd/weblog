package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.VisitorRecord;
import com.blog.mapper.VisitorRecordMapper;
import com.blog.service.IVisitorRecordService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 访客记录表 服务实现类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Service
public class VisitorRecordServiceImpl extends ServiceImpl<VisitorRecordMapper, VisitorRecord> implements IVisitorRecordService {

}
