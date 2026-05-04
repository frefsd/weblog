package com.blog.service;

import com.blog.vo.CategoryArticleCountVO;
import com.blog.vo.DashboardArticleStatisticsVO;
import com.blog.vo.TagArticleCountVO;

import java.util.List;
import java.util.Map;

/**
 * 后台仪表盘统计服务接口
 */
public interface IDashboardService {

    /**
     * 获取文章统计信息
     * @return
     */
    DashboardArticleStatisticsVO getArticleStatistics();

    /**
     * 获取发布文章统计信息（最近 7 天）
     * @return
     */
    List<Map<String, Object>> getPublishArticleStatistics();

    /**
     * 获取 PV 统计信息（最近 7 天）
     * @return
     */
    List<Map<String, Object>> getPVStatistics();

    /**
     * 获取文章分类统计（用于扇形图）
     * @return
     */
    List<CategoryArticleCountVO> getCategoryArticleStatistics();

    /**
     * 获取文章标签统计（用于柱状图）
     * @return
     */
    List<TagArticleCountVO> getTagArticleStatistics();
}
