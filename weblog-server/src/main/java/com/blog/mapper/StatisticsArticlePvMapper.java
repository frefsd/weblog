package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.StatisticsArticlePv;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * <p>
 * 统计表 - 文章 PV Mapper 接口
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
public interface StatisticsArticlePvMapper extends BaseMapper<StatisticsArticlePv> {

    /**
     * 原子 upsert：指定日期 PV 不存在则插入，存在则 +1
     */
    int upsertByDate(@Param("pvDate") LocalDate pvDate);
}
