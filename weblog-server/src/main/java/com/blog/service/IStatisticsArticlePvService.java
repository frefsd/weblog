package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.StatisticsArticlePv;

import java.time.LocalDate;

/**
 * <p>
 * 统计表 - 文章 PV 服务类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
public interface IStatisticsArticlePvService extends IService<StatisticsArticlePv> {

    /**
     * 更新每日 PV 计数（原子 upsert，不存在则插入，存在则 +1）
     */
    void upsertDailyPV(LocalDate date);
}
