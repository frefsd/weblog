package com.blog.vo;

import lombok.Data;

/**
 * 仪表盘 PV 统计 VO
 */
@Data
public class DashboardPVStatisticsVO {

    /**
     * 总阅读量
     */
    private Long totalPV;

    /**
     * 今日阅读量
     */
    private Long todayPV;

    /**
     * 昨日阅读量
     */
    private Long yesterdayPV;
}
