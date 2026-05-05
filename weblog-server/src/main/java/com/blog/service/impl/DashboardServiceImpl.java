package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.*;
import com.blog.mapper.*;
import com.blog.service.IDashboardService;
import com.blog.service.IStatisticsArticlePvService;
import com.blog.vo.CategoryArticleCountVO;
import com.blog.vo.DashboardArticleStatisticsVO;
import com.blog.vo.TagArticleCountVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘统计服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements IDashboardService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleCategoryRelMapper articleCategoryRelMapper;
    private final ArticleTagRelMapper articleTagRelMapper;
    private final IStatisticsArticlePvService statisticsArticlePvService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 获取文章统计信息
     */
    @Override
    public DashboardArticleStatisticsVO getArticleStatistics() {
        DashboardArticleStatisticsVO vo = new DashboardArticleStatisticsVO();

        // 查询文章总数
        Long totalArticles = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDeleted, 0));
        vo.setTotalArticles(totalArticles);

        // 查询今日发布文章数
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long todayArticles = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDeleted, 0)
                .ge(Article::getCreateTime, startOfDay)
                .le(Article::getCreateTime, endOfDay));
        vo.setTodayArticles(todayArticles);

        // 查询昨日发布文章数
        LocalDateTime startOfYesterday = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime endOfYesterday = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX);
        Long yesterdayArticles = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDeleted, 0)
                .ge(Article::getCreateTime, startOfYesterday)
                .le(Article::getCreateTime, endOfYesterday));
        vo.setYesterdayArticles(yesterdayArticles);

        // 查询分类总数
        Long totalCategories = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getIsDeleted, 0));
        vo.setTotalCategories(totalCategories);

        // 查询标签总数
        Long totalTags = tagMapper.selectCount(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getIsDeleted, 0));
        vo.setTotalTags(totalTags);

        // 计算总浏览量（所有文章的read_num之和）
        List<Article> allArticles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getIsDeleted, 0));
        Long totalPV = allArticles.stream()
                .mapToLong(Article::getReadNum)
                .sum();
        vo.setPvTotalCount(totalPV);
        
        // 设置前端期望的字段名（同时保持向后兼容）
        vo.setArticleTotalCount(totalArticles);
        vo.setCategoryTotalCount(totalCategories);
        vo.setTagTotalCount(totalTags);
        // 保持向后兼容：确保原有字段也有值（虽然已经设置过）
        vo.setTotalArticles(totalArticles);
        vo.setTotalCategories(totalCategories);
        vo.setTotalTags(totalTags);

        return vo;
    }

    /**
     * 获取发布文章统计信息（最近 7 天）
     */
    @Override
    public List<Map<String, Object>> getPublishArticleStatistics() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 获取最近 7 天的日期
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(DATE_FORMATTER);

            LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);

            Long count = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                    .eq(Article::getIsDeleted, 0)
                    .ge(Article::getCreateTime, startOfDay)
                    .le(Article::getCreateTime, endOfDay));

            Map<String, Object> map = new HashMap<>();
            map.put("date", dateStr);
            map.put("count", count);
            result.add(map);
        }

        return result;
    }

    /**
     * 获取 PV 统计信息（最近 7 天）
     * <p>
     * 从 statistics_article_pv 表按日期查询，比起原来按文章创建时间聚合更准确。
     */
    @Override
    public List<Map<String, Object>> getPVStatistics() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(DATE_FORMATTER);

            StatisticsArticlePv pv = statisticsArticlePvService.lambdaQuery()
                    .eq(StatisticsArticlePv::getPvDate, date)
                    .one();

            Long pvCount = (pv != null) ? pv.getPvCount() : 0L;

            Map<String, Object> map = new HashMap<>();
            map.put("date", dateStr);
            map.put("pv", pvCount);
            result.add(map);
        }

        return result;
    }

    /**
     * 获取文章分类统计（用于扇形图）
     */
    @Override
    public List<CategoryArticleCountVO> getCategoryArticleStatistics() {
        // 查询所有分类
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getIsDeleted, 0));

        List<CategoryArticleCountVO> result = new ArrayList<>();

        for (Category category : categories) {
            // 查询每个分类下的文章数量
            List<ArticleCategoryRel> rels = articleCategoryRelMapper.selectList(
                    new LambdaQueryWrapper<ArticleCategoryRel>()
                            .eq(ArticleCategoryRel::getCategoryId, category.getId()));

            List<Long> articleIds = rels.stream()
                    .map(ArticleCategoryRel::getArticleId)
                    .collect(Collectors.toList());

            Long articleCount = 0L;
            if (!articleIds.isEmpty()) {
                articleCount = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                        .eq(Article::getIsDeleted, 0)
                        .in(Article::getId, articleIds));
            }

            CategoryArticleCountVO vo = new CategoryArticleCountVO();
            vo.setCategoryId(category.getId());
            vo.setCategoryName(category.getName());
            vo.setArticleCount(articleCount);
            result.add(vo);
        }

        return result;
    }

    /**
     * 获取文章标签统计（用于柱状图）
     */
    @Override
    public List<TagArticleCountVO> getTagArticleStatistics() {
        // 查询所有标签
        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getIsDeleted, 0));

        List<TagArticleCountVO> result = new ArrayList<>();

        for (Tag tag : tags) {
            // 查询每个标签下的文章数量
            List<ArticleTagRel> rels = articleTagRelMapper.selectList(
                    new LambdaQueryWrapper<ArticleTagRel>()
                            .eq(ArticleTagRel::getTagId, tag.getId()));

            List<Long> articleIds = rels.stream()
                    .map(ArticleTagRel::getArticleId)
                    .collect(Collectors.toList());

            Long articleCount = 0L;
            if (!articleIds.isEmpty()) {
                articleCount = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                        .eq(Article::getIsDeleted, 0)
                        .in(Article::getId, articleIds));
            }

            TagArticleCountVO vo = new TagArticleCountVO();
            vo.setTagId(tag.getId());
            vo.setTagName(tag.getName());
            vo.setArticleCount(articleCount);
            result.add(vo);
        }

        return result;
    }
}
