package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.StatisticsArticlePv;
import com.blog.mapper.StatisticsArticlePvMapper;
import com.blog.service.IStatisticsArticlePvService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 统计表 - 文章 PV 服务实现类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Service
public class StatisticsArticlePvServiceImpl extends ServiceImpl<StatisticsArticlePvMapper, StatisticsArticlePv> implements IStatisticsArticlePvService {

}
