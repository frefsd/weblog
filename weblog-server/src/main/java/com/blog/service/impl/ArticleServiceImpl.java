package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.dto.*;
import com.blog.entity.*;
import com.blog.ai.event.ArticleChangeEvent;
import com.blog.exception.BusinessException;
import com.blog.mapper.*;
import com.blog.service.IArticleService;
import com.blog.service.IStatisticsArticlePvService;
import com.blog.service.IVisitorRecordService;
import com.blog.utils.RedisConstants;
import com.blog.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 文章表 服务实现类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleContentMapper articleContentMapper;
    private final ArticleCategoryRelMapper articleCategoryRelMapper;
    private final ArticleTagRelMapper articleTagRelMapper;
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;
    private final IVisitorRecordService visitorRecordService;
    private final IStatisticsArticlePvService statisticsArticlePvService;
    private final ApplicationEventPublisher eventPublisher;

    // 自注入，用于 Spring Cache AOP 代理调用
    @Autowired
    @Lazy
    private ArticleServiceImpl self;

    private static final DateTimeFormatter FRONTEND_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static LocalDateTime parseFrontendDateTime(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty() || "{}".equals(v) || "null".equalsIgnoreCase(v)) return null;
        return LocalDateTime.parse(v, FRONTEND_DT);
    }

    /**
     * 获取文章详情
     *
     * @param query
     * @return
     */
    @Override
    public ArticleDetailVO getArticleDetailForAdmin(ArticleDetailDTO query) {
        Long articleId = query.getArticleId();
        Article article = articleMapper.selectById(articleId);
        if (article == null || (article.getIsDeleted() != null && article.getIsDeleted() == 1)) {
            throw new BusinessException("文章不存在");
        }

        ArticleContent content = articleContentMapper.selectOne(
                new LambdaQueryWrapper<ArticleContent>().eq(ArticleContent::getArticleId, articleId)
        );
        ArticleCategoryRel categoryRel = articleCategoryRelMapper.selectOne(
                new LambdaQueryWrapper<ArticleCategoryRel>().eq(ArticleCategoryRel::getArticleId, articleId)
        );
        List<ArticleTagRel> tagRels = articleTagRelMapper.selectList(
                new LambdaQueryWrapper<ArticleTagRel>().eq(ArticleTagRel::getArticleId, articleId)
        );

        List<Long> tagIds = (tagRels == null || tagRels.isEmpty())
                ? Collections.emptyList()
                : tagRels.stream().map(ArticleTagRel::getTagId).collect(Collectors.toList());

        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setTitleImage(article.getTitleImage());
        vo.setDescription(article.getDescription());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());

        vo.setContent(content == null ? "" : content.getContent());
        vo.setCategoryId(categoryRel == null ? null : categoryRel.getCategoryId());
        vo.setTagIds(tagIds);

        return vo;
    }

    /**
     * 文章分页查询
     *
     * @param query
     * @return
     */
    @Override
    public IPage<Article> getArticlePageList(ArticlePageDTO query) {
        long current = query.getCurrent();
        long size = query.getSize();
        long offset = (current - 1) * size;

        LocalDateTime start = parseFrontendDateTime(query.getStartDate());
        LocalDateTime end = parseFrontendDateTime(query.getEndDate());
        String searchTitle = query.getSearchTitle();

        LambdaQueryWrapper<Article> base = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDeleted, 0)
                .like(searchTitle != null && !searchTitle.trim().isEmpty(), Article::getTitle, searchTitle.trim())
                .ge(start != null, Article::getCreateTime, start)
                .le(end != null, Article::getCreateTime, end);

        long total = this.count(base);
        List<Article> records = total == 0
                ? List.of()
                : this.list(base.orderByDesc(Article::getCreateTime).last("LIMIT " + offset + "," + size));

        Page<Article> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    private Tag getOrCreateTagByName(String name) {
        Tag exist = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, name)
                .eq(Tag::getIsDeleted, 0));
        if (exist != null) return exist;

        Tag tag = new Tag();
        tag.setName(name);
        tag.setIsDeleted(0);
        tagMapper.insert(tag);
        return tag;
    }

    private List<Long> resolveTagIds(List<Object> tags) {
        if (tags == null || tags.isEmpty()) {
            throw new BusinessException("请选择文章标签");
        }

        Set<Long> ids = new HashSet<>();
        for (Object t : tags) {
            if (t == null) continue;
            if (t instanceof Number n) {
                ids.add(n.longValue());
                continue;
            }
            String s = String.valueOf(t).trim();
            if (s.isEmpty()) continue;
            // 纯数字 -> 视为 tagId；否则视为新标签名
            if (s.matches("^\\d+$")) {
                ids.add(Long.parseLong(s));
            } else {
                Tag tag = getOrCreateTagByName(s);
                ids.add(tag.getId());
            }
        }

        if (ids.isEmpty()) {
            throw new BusinessException("请选择文章标签");
        }
        return ids.stream().toList();
    }

    private void ensureCategoryExists(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException("请选择文章分类");
        }
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || (category.getIsDeleted() != null && category.getIsDeleted() == 1)) {
            throw new BusinessException("文章分类不存在");
        }
    }

    /**
     * 发布文章
     *
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {RedisConstants.ARTICLE_DETAIL_CACHE, RedisConstants.ARTICLE_INDEX_CACHE}, allEntries = true)
    public void publishArticle(ArticlePublishDTO dto) {
        ensureCategoryExists(dto.getCategoryId());
        List<Long> tagIds = resolveTagIds(dto.getTags());

        Article article = new Article();
        article.setTitle(dto.getTitle().trim());
        article.setTitleImage(dto.getTitleImage().trim());
        article.setDescription(dto.getDescription().trim());
        article.setIsDeleted(0);
        article.setReadNum(1);
        articleMapper.insert(article);

        Long articleId = article.getId();
        if (articleId == null) {
            throw new BusinessException("发布失败：文章ID生成失败");
        }

        ArticleContent content = new ArticleContent();
        content.setArticleId(articleId);
        content.setContent(dto.getContent());
        articleContentMapper.insert(content);

        ArticleCategoryRel categoryRel = new ArticleCategoryRel();
        categoryRel.setArticleId(articleId);
        categoryRel.setCategoryId(dto.getCategoryId());
        articleCategoryRelMapper.insert(categoryRel);

        for (Long tagId : tagIds) {
            ArticleTagRel rel = new ArticleTagRel();
            rel.setArticleId(articleId);
            rel.setTagId(tagId);
            articleTagRelMapper.insert(rel);
        }

        eventPublisher.publishEvent(new ArticleChangeEvent(articleId, dto.getTitle(), dto.getContent(), ArticleChangeEvent.ChangeType.CREATED));
    }

    /**
     * 修改文章
     *
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {RedisConstants.ARTICLE_DETAIL_CACHE, RedisConstants.ARTICLE_INDEX_CACHE}, allEntries = true)
    public void updateArticle(ArticleUpdateDTO dto) {
        ensureCategoryExists(dto.getCategoryId());
        List<Long> tagIds = resolveTagIds(dto.getTags());

        Article exist = articleMapper.selectById(dto.getId());
        if (exist == null || (exist.getIsDeleted() != null && exist.getIsDeleted() == 1)) {
            throw new BusinessException("文章不存在");
        }

        Article article = new Article();
        article.setId(dto.getId());
        article.setTitle(dto.getTitle().trim());
        article.setTitleImage(dto.getTitleImage().trim());
        article.setDescription(dto.getDescription().trim());
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);

        ArticleContent content = articleContentMapper.selectOne(
                new LambdaQueryWrapper<ArticleContent>().eq(ArticleContent::getArticleId, dto.getId())
        );
        if (content == null) {
            content = new ArticleContent();
            content.setArticleId(dto.getId());
            content.setContent(dto.getContent());
            articleContentMapper.insert(content);
        } else {
            ArticleContent update = new ArticleContent();
            update.setId(content.getId());
            update.setContent(dto.getContent());
            articleContentMapper.updateById(update);
        }

        ArticleCategoryRel categoryRel = articleCategoryRelMapper.selectOne(
                new LambdaQueryWrapper<ArticleCategoryRel>().eq(ArticleCategoryRel::getArticleId, dto.getId())
        );
        if (categoryRel == null) {
            categoryRel = new ArticleCategoryRel();
            categoryRel.setArticleId(dto.getId());
            categoryRel.setCategoryId(dto.getCategoryId());
            articleCategoryRelMapper.insert(categoryRel);
        } else {
            ArticleCategoryRel update = new ArticleCategoryRel();
            update.setId(categoryRel.getId());
            update.setCategoryId(dto.getCategoryId());
            articleCategoryRelMapper.updateById(update);
        }

        // 重建标签关系
        articleTagRelMapper.delete(new LambdaQueryWrapper<ArticleTagRel>().eq(ArticleTagRel::getArticleId, dto.getId()));
        for (Long tagId : tagIds) {
            ArticleTagRel rel = new ArticleTagRel();
            rel.setArticleId(dto.getId());
            rel.setTagId(tagId);
            articleTagRelMapper.insert(rel);
        }

        eventPublisher.publishEvent(new ArticleChangeEvent(dto.getId(), dto.getTitle(), dto.getContent(), ArticleChangeEvent.ChangeType.UPDATED));
    }

    /**
     * 删除文章
     *
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {RedisConstants.ARTICLE_DETAIL_CACHE, RedisConstants.ARTICLE_INDEX_CACHE}, allEntries = true)
    public void deleteArticle(ArticleDeleteDTO dto) {
        Long articleId = dto.getArticleId();
        Article exist = articleMapper.selectById(articleId);
        if (exist == null || (exist.getIsDeleted() != null && exist.getIsDeleted() == 1)) {
            throw new BusinessException("文章不存在");
        }
        Article update = new Article();
        update.setId(articleId);
        update.setIsDeleted(1);
        update.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(update);

        eventPublisher.publishEvent(new ArticleChangeEvent(articleId, exist.getTitle(), null, ArticleChangeEvent.ChangeType.DELETED));
    }

    /**
     * 获取前台首页文章分页列表（缓存 5 分钟，避免 N+1 查询）
     */
    @Override
    @Cacheable(value = RedisConstants.ARTICLE_INDEX_CACHE, key = "#query.current + '_' + #query.size", unless = "#result == null or #result.records.isEmpty()")
    public IPage<ArticleIndexVO> getArticleIndexPage(ArticlePageDTO query) {
        long current = query.getCurrent();
        long size = query.getSize();
        long offset = (current - 1) * size;

        // 查询文章列表
        LambdaQueryWrapper<Article> base = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDeleted, 0)
                .orderByDesc(Article::getCreateTime);

        long total = this.count(base);
        List<Article> articleList = total == 0
                ? List.of()
                : this.list(base.last("LIMIT " + offset + "," + size));

        // 转换为 VO
        List<ArticleIndexVO> voList = articleList.stream()
                .map(this::convertToArticleIndexVO)
                .collect(Collectors.toList());

        Page<ArticleIndexVO> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(voList);
        return page;
    }

    /**
     * 获取前台文章详情（readNum 始终自增，不走缓存）
     */
    @Override
    public ArticleFrontendDetailVO getArticleDetailForFrontend(Long articleId, HttpServletRequest request) {
        // 1. 查询文章基本信息（用于 readNum 自增）
        Article article = articleMapper.selectById(articleId);
        if (article == null || (article.getIsDeleted() != null && article.getIsDeleted() == 1)) {
            throw new BusinessException("文章不存在");
        }

        // 2. 自增阅读数（始终写入数据库，保证实时准确）
        article.setReadNum(article.getReadNum() + 1);
        articleMapper.updateById(article);

        // 3. 异步记录访客信息与日 PV（不阻塞响应）
        visitorRecordService.recordVisitorAsync(request);
        statisticsArticlePvService.upsertDailyPV(LocalDate.now());

        // 4. 通过 self 调用 @Cacheable 方法获取缓存详情
        ArticleFrontendDetailVO detail = self.getCachedArticleDetail(articleId);

        // 5. 用当前 readNum 覆盖缓存中的旧值
        detail.setReadNum(article.getReadNum());
        return detail;
    }

    /**
     * 获取缓存的文章详情（仅缓存静态数据，readNum 由外层方法处理）
     * <p>
     * 缓存 30 分钟，内容/分类/标签/上下篇等数据很少变化。
     */
    @Cacheable(value = RedisConstants.ARTICLE_DETAIL_CACHE, key = "#articleId", unless = "#result == null")
    @Override
    public ArticleFrontendDetailVO getCachedArticleDetail(Long articleId) {
        // 查询文章基本信息
        Article article = articleMapper.selectById(articleId);

        // 查询文章内容
        ArticleContent content = articleContentMapper.selectOne(
                new LambdaQueryWrapper<ArticleContent>().eq(ArticleContent::getArticleId, articleId)
        );

        // 查询分类
        ArticleCategoryRel categoryRel = articleCategoryRelMapper.selectOne(
                new LambdaQueryWrapper<ArticleCategoryRel>().eq(ArticleCategoryRel::getArticleId, articleId)
        );
        Category category = null;
        if (categoryRel != null && categoryRel.getCategoryId() != null) {
            category = categoryMapper.selectById(categoryRel.getCategoryId());
        }

        // 查询标签
        List<ArticleTagRel> tagRels = articleTagRelMapper.selectList(
                new LambdaQueryWrapper<ArticleTagRel>().eq(ArticleTagRel::getArticleId, articleId)
        );
        List<TagSimpleVO> tagVOList = tagRels.stream()
                .map(rel -> {
                    Tag tag = tagMapper.selectById(rel.getTagId());
                    if (tag != null) {
                        TagSimpleVO vo = new TagSimpleVO();
                        vo.setId(tag.getId());
                        vo.setName(tag.getName());
                        return vo;
                    }
                    return null;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        // 查询上下篇文章
        ArticleBriefVO preArticle = null;
        ArticleBriefVO nextArticle = null;

        // 上一篇：创建时间早于当前文章
        Article pre = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDeleted, 0)
                .lt(Article::getCreateTime, article.getCreateTime())
                .orderByDesc(Article::getCreateTime)
                .last("LIMIT 1"));
        if (pre != null) {
            preArticle = new ArticleBriefVO();
            preArticle.setId(pre.getId());
            preArticle.setTitle(pre.getTitle());
        }

        // 下一篇：创建时间晚于当前文章
        Article next = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDeleted, 0)
                .gt(Article::getCreateTime, article.getCreateTime())
                .orderByAsc(Article::getCreateTime)
                .last("LIMIT 1"));
        if (next != null) {
            nextArticle = new ArticleBriefVO();
            nextArticle.setId(next.getId());
            nextArticle.setTitle(next.getTitle());
        }

        // 组装 VO
        ArticleFrontendDetailVO vo = new ArticleFrontendDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContent(content == null ? "" : content.getContent());
        vo.setCreateTime(article.getCreateTime().format(FRONTEND_DT));
        vo.setUpdateTime(article.getUpdateTime() != null ? article.getUpdateTime().format(FRONTEND_DT) : null);
        vo.setReadNum(article.getReadNum());
        vo.setCategoryId(category != null ? category.getId() : null);
        vo.setCategoryName(category != null ? category.getName() : "");
        vo.setTags(tagVOList);
        vo.setPreArticle(preArticle);
        vo.setNextArticle(nextArticle);

        return vo;
    }

    /**
     * 搜索文章（前台）
     */
    @Override
    public IPage<ArticleIndexVO> searchArticles(ArticleSearchDTO dto) {
        long current = dto.getCurrent() != null ? dto.getCurrent() : 1L;
        long size = dto.getSize() != null ? dto.getSize() : 10L;
        String keyword = dto.getKeyword();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果关键词为空，返回空结果
            Page<ArticleIndexVO> emptyPage = new Page<>(current, size);
            emptyPage.setTotal(0);
            emptyPage.setRecords(List.of());
            return emptyPage;
        }
        
        keyword = keyword.trim();
        log.info("搜索关键词: {}", keyword);
        
        // 构建查询条件：标题模糊匹配，且文章未删除
        // 修复查询条件：确保 is_deleted=0 同时应用于标题和描述搜索
        final String searchKeyword = keyword; // 创建final变量用于lambda表达式
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDeleted, 0)
                .and(wrapper -> 
                    wrapper.like(Article::getTitle, searchKeyword)
                           .or()
                           .like(Article::getDescription, searchKeyword)
                )
                .orderByDesc(Article::getCreateTime);
        
        log.info("查询条件: {}", queryWrapper.getCustomSqlSegment());
        
        // 使用分页插件
        Page<Article> pageParam = new Page<>(current, size);
        log.info("分页参数: current={}, size={}", current, size);
        Page<Article> articlePage = this.page(pageParam, queryWrapper);
        
        log.info("搜索结果: 总数={}, 当前页记录数={}, 总页数={}", 
                articlePage.getTotal(), 
                articlePage.getRecords().size(),
                articlePage.getPages());
        
        // 调试：直接查询总数
        Long totalCount = this.count(queryWrapper);
        log.info("直接查询总数: {}", totalCount);
        
        // 修复分页总数问题：如果page查询的total为0但实际有数据，使用count查询的结果
        long finalTotal = articlePage.getTotal();
        if (finalTotal == 0 && totalCount != null && totalCount > 0) {
            finalTotal = totalCount;
            log.info("修正分页总数: {}", finalTotal);
        }
        
        // 转换为 VO
        List<ArticleIndexVO> voList = articlePage.getRecords().stream()
                .map(this::convertToArticleIndexVO)
                .collect(Collectors.toList());
        
        Page<ArticleIndexVO> resultPage = new Page<>(current, size);
        resultPage.setTotal(finalTotal);
        resultPage.setRecords(voList);
        // 计算总页数
        resultPage.setPages((long) Math.ceil((double) finalTotal / size));
        
        return resultPage;
    }

    /**
     * 根据分类 ID 获取文章列表
     */
    @Override
    public IPage<ArticleIndexVO> getArticlesByCategoryId(Long categoryId, ArticlePageDTO query) {
        long current = query.getCurrent();
        long size = query.getSize();
        long offset = (current - 1) * size;

        LambdaQueryWrapper<Article> base = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDeleted, 0)
                .inSql(Article::getId, "SELECT article_id FROM article_category_rel WHERE category_id = " + categoryId)
                .orderByDesc(Article::getCreateTime);

        Page<Article> pageParam = new Page<>(current, size);
        // 如果使用 MP 分页插件，直接调用 page 方法，不要手动算 offset
        Page<Article> articlePage = this.page(pageParam, base);

        // 4. 转换为 VO
        List<ArticleIndexVO> voList = articlePage.getRecords().stream()
                .map(this::convertToArticleIndexVO)
                .collect(Collectors.toList());

        // 5. 构造返回结果
        Page<ArticleIndexVO> resultPage = new Page<>(current, size);
        resultPage.setTotal(articlePage.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }

    /**
     * 根据标签 ID 获取文章列表
     */
    @Override
    public IPage<ArticleIndexVO> getArticlesByTagId(Long tagId, ArticlePageDTO query) {
        long current = query.getCurrent();
        long size = query.getSize();
        long offset = (current - 1) * size;

        // 查询文章列表
        LambdaQueryWrapper<Article> base = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDeleted, 0)
                .inSql(Article::getId, "SELECT article_id FROM article_tag_rel WHERE tag_id = " + tagId)
                .orderByDesc(Article::getCreateTime);

        long total = this.count(base);
        List<Article> articleList = total == 0
                ? List.of()
                : this.list(base.last("LIMIT " + offset + "," + size));

        // 转换为 VO
        List<ArticleIndexVO> voList = articleList.stream()
                .map(this::convertToArticleIndexVO)
                .collect(Collectors.toList());

        Page<ArticleIndexVO> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(voList);
        return page;
    }

    /**
     * 获取归档列表（年→月二级分组，含分类名称）
     */
    @Override
    public List<ArchiveYearVO> getArchiveList() {
        // 查询所有文章（按创建时间降序）
        List<Article> articleList = this.list(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDeleted, 0)
                .orderByDesc(Article::getCreateTime));

        // 预加载所有文章的分类关联（避免 N+1）
        Set<Long> articleIds = articleList.stream().map(Article::getId).collect(Collectors.toSet());
        List<ArticleCategoryRel> allRels = articleIds.isEmpty() ? Collections.emptyList() :
                articleCategoryRelMapper.selectList(new LambdaQueryWrapper<ArticleCategoryRel>()
                        .in(ArticleCategoryRel::getArticleId, articleIds));
        Set<Long> categoryIds = allRels.stream().map(ArticleCategoryRel::getCategoryId).collect(Collectors.toSet());
        List<Category> categories = categoryIds.isEmpty() ? Collections.emptyList() :
                categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .in(Category::getId, categoryIds));

        // 构建 articleId → categoryName 映射
        java.util.Map<Long, String> categoryNameMap = new java.util.HashMap<>();
        for (ArticleCategoryRel rel : allRels) {
            categories.stream()
                    .filter(c -> c.getId().equals(rel.getCategoryId()))
                    .findFirst()
                    .ifPresent(c -> categoryNameMap.put(rel.getArticleId(), c.getName()));
        }

        // 年→月二级分组
        java.util.Map<String, ArchiveYearVO> yearMap = new java.util.LinkedHashMap<>();
        DateTimeFormatter yearFmt = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MM月");
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("MM-dd");

        for (Article article : articleList) {
            String year = article.getCreateTime().format(yearFmt);
            String month = article.getCreateTime().format(monthFmt);

            // 获取或创建年份
            ArchiveYearVO yearVO = yearMap.computeIfAbsent(year, k -> {
                ArchiveYearVO vo = new ArchiveYearVO();
                vo.setYear(k);
                vo.setArticleCount(0);
                vo.setMonths(new java.util.ArrayList<>());
                return vo;
            });

            // 获取或创建月份
            ArchiveMonthVO monthVO = null;
            for (ArchiveMonthVO m : yearVO.getMonths()) {
                if (m.getMonth().equals(month)) {
                    monthVO = m;
                    break;
                }
            }
            if (monthVO == null) {
                monthVO = new ArchiveMonthVO();
                monthVO.setMonth(month);
                monthVO.setArticleCount(0);
                monthVO.setArticles(new java.util.ArrayList<>());
                yearVO.getMonths().add(monthVO);
            }

            // 构建文章简要信息
            ArticleBriefVO brief = new ArticleBriefVO();
            brief.setId(article.getId());
            brief.setTitle(article.getTitle());
            brief.setTitleImage(article.getTitleImage());
            brief.setCreateTime(article.getCreateTime().format(dayFmt));
            brief.setCategoryName(categoryNameMap.getOrDefault(article.getId(), null));

            monthVO.getArticles().add(brief);
            monthVO.setArticleCount(monthVO.getArticleCount() + 1);
            yearVO.setArticleCount(yearVO.getArticleCount() + 1);
        }

        return new java.util.ArrayList<>(yearMap.values());
    }

    /**
     * 转换为 ArticleIndexVO
     */
    private ArticleIndexVO convertToArticleIndexVO(Article article) {
        ArticleIndexVO vo = new ArticleIndexVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setTitleImage(article.getTitleImage());
        vo.setDescription(article.getDescription());
        vo.setCreateTime(article.getCreateTime().format(FRONTEND_DT));
        vo.setReadNum(article.getReadNum());

        // 查询分类
        ArticleCategoryRel categoryRel = articleCategoryRelMapper.selectOne(
                new LambdaQueryWrapper<ArticleCategoryRel>()
                        .eq(ArticleCategoryRel::getArticleId, article.getId())
        );
        if (categoryRel != null && categoryRel.getCategoryId() != null) {
            Category category = categoryMapper.selectById(categoryRel.getCategoryId());
            if (category != null) {
                CategorySimpleVO categoryVO = new CategorySimpleVO();
                categoryVO.setId(category.getId());
                categoryVO.setName(category.getName());
                vo.setCategory(categoryVO);
            }
        }

        // 查询标签
        List<ArticleTagRel> tagRels = articleTagRelMapper.selectList(
                new LambdaQueryWrapper<ArticleTagRel>()
                        .eq(ArticleTagRel::getArticleId, article.getId())
        );
        List<TagSimpleVO> tagVOList = tagRels.stream()
                .map(rel -> {
                    Tag tag = tagMapper.selectById(rel.getTagId());
                    if (tag != null) {
                        TagSimpleVO tagVO = new TagSimpleVO();
                        tagVO.setId(tag.getId());
                        tagVO.setName(tag.getName());
                        return tagVO;
                    }
                    return null;
                })
                .filter(v -> v != null)
                .collect(Collectors.toList());
        vo.setTags(tagVOList);

        return vo;
    }
}
