package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.dto.TagAddDTO;
import com.blog.dto.TagDeleteDTO;
import com.blog.dto.TagPageDTO;
import com.blog.dto.TagSearchDTO;
import com.blog.entity.Article;
import com.blog.entity.ArticleTagRel;
import com.blog.entity.Tag;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagRelMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.ITagService;
import com.blog.utils.RedisConstants;
import com.blog.vo.SelectOptionVO;
import com.blog.vo.TagSimpleVO;
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
 * 标签表 服务实现类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

    private final ArticleTagRelMapper articleTagRelMapper;
    private final ArticleMapper articleMapper;

    private static final DateTimeFormatter FRONTEND_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static LocalDateTime parseFrontendDateTime(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty() || "{}".equals(v) || "null".equalsIgnoreCase(v)) return null;
        return LocalDateTime.parse(v, FRONTEND_DT);
    }

    /**
     * 添加标签
     *
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisConstants.TAG_LIST_CACHE, allEntries = true)
    public void addTags(TagAddDTO dto) {
        if (dto.getTags() == null || dto.getTags().isEmpty()) {
            throw new BusinessException("tags不能为空");
        }
        for (String raw : dto.getTags()) {
            if (raw == null) continue;
            String name = raw.trim();
            if (name.isEmpty()) continue;

            Tag exist = this.getOne(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getName, name)
                    .eq(Tag::getIsDeleted, 0));
            if (exist != null) continue;

            Tag tag = new Tag();
            tag.setName(name);
            tag.setIsDeleted(0);
            this.save(tag);
        }
    }

    /**
     * 分页查询标签
     *
     * @param dto
     * @return
     */
    @Override
    public IPage<Tag> pageTags(TagPageDTO dto) {
        LocalDateTime start = parseFrontendDateTime(dto.getStartDate());
        LocalDateTime end = parseFrontendDateTime(dto.getEndDate());
        String tagName = dto.getTagName();

        long current = dto.getCurrent();
        long size = dto.getSize();
        long offset = (current - 1) * size;

        LambdaQueryWrapper<Tag> base = new LambdaQueryWrapper<Tag>()
                .eq(Tag::getIsDeleted, 0)
                .like(tagName != null && !tagName.trim().isEmpty(), Tag::getName, tagName.trim())
                .ge(start != null, Tag::getCreateTime, start)
                .le(end != null, Tag::getCreateTime, end);

        long total = this.count(base);
        List<Tag> records = total == 0
                ? List.of()
                : this.list(base.orderByDesc(Tag::getCreateTime).last("LIMIT " + offset + "," + size));

        Page<Tag> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    /**
     * 删除标签
     *
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisConstants.TAG_LIST_CACHE, allEntries = true)
    public void deleteTag(TagDeleteDTO dto) {
        Long tagId = dto.getTagId();

        // 校验：标签被未删除文章引用则禁止删除
        List<Long> articleIds = articleTagRelMapper.selectList(
                        new LambdaQueryWrapper<ArticleTagRel>()
                                .eq(ArticleTagRel::getTagId, tagId)
                )
                .stream()
                .map(ArticleTagRel::getArticleId)
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
                throw new BusinessException("该标签被文章使用，无法删除");
            }
        }

        Tag tag = this.getById(dto.getTagId());
        if (tag == null || (tag.getIsDeleted() != null && tag.getIsDeleted() == 1)) {
            throw new BusinessException("标签不存在");
        }
        Tag update = new Tag();
        update.setId(dto.getTagId());
        update.setIsDeleted(1);
        update.setUpdateTime(LocalDateTime.now());
        this.updateById(update);
    }

    /**
     * 标签下拉列表
     *
     * @return
     */
    @Override
    public List<SelectOptionVO> selectList() {
        return this.list(new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getIsDeleted, 0)
                        .orderByDesc(Tag::getCreateTime))
                .stream()
                .map(t -> new SelectOptionVO(t.getId(), t.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 标签搜索
     *
     * @param dto
     * @return
     */
    @Override
    public List<SelectOptionVO> search(TagSearchDTO dto) {
        String key = dto == null ? null : dto.getKey();
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>()
                .eq(Tag::getIsDeleted, 0)
                .orderByDesc(Tag::getCreateTime);
        if (key != null && !key.trim().isEmpty()) {
            wrapper.like(Tag::getName, key.trim());
        }
        return this.list(wrapper).stream()
                .filter(Objects::nonNull)
                .map(t -> new SelectOptionVO(t.getId(), t.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 获取全部标签（前台使用）
     */
    @Override
    @Cacheable(value = RedisConstants.TAG_LIST_CACHE, unless = "#result == null or #result.isEmpty()")
    public List<TagSimpleVO> getAllTags() {
        return this.list(new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getIsDeleted, 0)
                        .orderByDesc(Tag::getCreateTime))
                .stream()
                .map(t -> {
                    TagSimpleVO vo = new TagSimpleVO();
                    vo.setId(t.getId());
                    vo.setName(t.getName());
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
