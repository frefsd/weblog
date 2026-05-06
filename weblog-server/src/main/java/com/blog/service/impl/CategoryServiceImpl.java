package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.dto.CategoryAddDTO;
import com.blog.dto.CategoryDeleteDTO;
import com.blog.dto.CategoryPageDTO;
import com.blog.dto.CategorySearchDTO;
import com.blog.entity.Article;
import com.blog.entity.ArticleCategoryRel;
import com.blog.entity.Category;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleCategoryRelMapper;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.service.ICategoryService;
import com.blog.utils.RedisConstants;
import com.blog.vo.CategorySimpleVO;
import com.blog.vo.SelectOptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * <p>
 * 文章分类表 服务实现类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    private final ArticleCategoryRelMapper articleCategoryRelMapper;
    private final ArticleMapper articleMapper;

    private static final DateTimeFormatter FRONTEND_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static LocalDateTime parseFrontendDateTime(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty() || "{}".equals(v) || "null".equalsIgnoreCase(v)) return null;
        return LocalDateTime.parse(v, FRONTEND_DT);
    }

    /**
     * 添加分类
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisConstants.CATEGORY_LIST_CACHE, allEntries = true)
    public void addCategory(CategoryAddDTO dto) {
        String name = dto.getName() == null ? "" : dto.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException("分类名称不能为空");
        }

        Category exist = this.getOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name)
                .eq(Category::getIsDeleted, 0));
        if (exist != null) {
            throw new BusinessException("分类已存在");
        }

        Category category = new Category();
        category.setName(name);
        category.setIsDeleted(0);
        this.save(category);
    }

    /**
     * 分页查询文章分类
     * @param dto
     * @return
     */
    @Override
    public IPage<Category> pageCategories(CategoryPageDTO dto) {
        LocalDateTime start = parseFrontendDateTime(dto.getStartDate());
        LocalDateTime end = parseFrontendDateTime(dto.getEndDate());
        String categoryName = dto.getCategoryName();

        long current = dto.getCurrent();
        long size = dto.getSize();
        long offset = (current - 1) * size;

        LambdaQueryWrapper<Category> base = new LambdaQueryWrapper<Category>()
                .eq(Category::getIsDeleted, 0)
                .like(categoryName != null && !categoryName.trim().isEmpty(), Category::getName, categoryName.trim())
                .ge(start != null, Category::getCreateTime, start)
                .le(end != null, Category::getCreateTime, end);

        long total = this.count(base);
        List<Category> records = total == 0
                ? List.of()
                : this.list(base.orderByDesc(Category::getCreateTime).last("LIMIT " + offset + "," + size));

        Page<Category> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    /**
     * 删除分类
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisConstants.CATEGORY_LIST_CACHE, allEntries = true)
    public void deleteCategory(CategoryDeleteDTO dto) {
        Long categoryId = dto.getCategoryId();

        // 校验：分类下存在未删除文章则禁止删除
        List<Long> articleIds = articleCategoryRelMapper.selectList(
                        new LambdaQueryWrapper<ArticleCategoryRel>()
                                .eq(ArticleCategoryRel::getCategoryId, categoryId)
                )
                .stream()
                .map(ArticleCategoryRel::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (!articleIds.isEmpty()) {
            Long activeCount = articleMapper.selectCount(
                    new LambdaQueryWrapper<Article>()
                            .in(Article::getId, articleIds)
                            .eq(Article::getIsDeleted, 0)
            );
            if (activeCount != null && activeCount > 0) {
                throw new BusinessException("该分类下存在文章，无法删除");
            }
        }

        Category category = this.getById(dto.getCategoryId());
        if (category == null || (category.getIsDeleted() != null && category.getIsDeleted() == 1)) {
            throw new BusinessException("分类不存在");
        }
        Category update = new Category();
        update.setId(dto.getCategoryId());
        update.setIsDeleted(1);
        update.setUpdateTime(LocalDateTime.now());
        this.updateById(update);
    }

    /**
     * 获取分类列表
     * @return
     */
    @Override
    public List<SelectOptionVO> selectList() {
        return this.list(new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsDeleted, 0)
                        .orderByDesc(Category::getCreateTime))
                .stream()
                .map(c -> new SelectOptionVO(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 分类搜索
     * @param dto
     * @return
     */
    @Override
    public List<SelectOptionVO> search(CategorySearchDTO dto) {
        String key = dto == null ? null : dto.getKey();
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getIsDeleted, 0)
                .orderByDesc(Category::getCreateTime);
        if (key != null && !key.trim().isEmpty()) {
            wrapper.like(Category::getName, key.trim());
        }
        return this.list(wrapper).stream()
                .filter(Objects::nonNull)
                .map(c -> new SelectOptionVO(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 获取全部分类（前台使用）
     */
    @Override
    @Cacheable(value = RedisConstants.CATEGORY_LIST_CACHE, unless = "#result == null or #result.isEmpty()")
    public List<CategorySimpleVO> getAllCategories() {
        return this.list(new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsDeleted, 0)
                        .orderByDesc(Category::getCreateTime))
                .stream()
                .map(c -> {
                    CategorySimpleVO vo = new CategorySimpleVO();
                    vo.setId(c.getId());
                    vo.setName(c.getName());
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
