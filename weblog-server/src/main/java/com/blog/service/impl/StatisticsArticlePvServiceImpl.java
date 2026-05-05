package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.StatisticsArticlePv;
import com.blog.mapper.StatisticsArticlePvMapper;
import com.blog.service.IStatisticsArticlePvService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * <p>
 * 统计表 - 文章 PV 服务实现类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Service
@RequiredArgsConstructor
public class StatisticsArticlePvServiceImpl extends ServiceImpl<StatisticsArticlePvMapper, StatisticsArticlePv> implements IStatisticsArticlePvService {

    private final StatisticsArticlePvMapper statisticsArticlePvMapper;

    @Override
    public void upsertDailyPV(LocalDate date) {
        statisticsArticlePvMapper.upsertByDate(date);
    }
}
