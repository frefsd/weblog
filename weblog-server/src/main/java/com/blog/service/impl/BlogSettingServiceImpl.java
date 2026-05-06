package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.dto.BlogSettingDTO;
import com.blog.entity.BlogSetting;
import com.blog.exception.BusinessException;
import com.blog.mapper.BlogSettingMapper;
import com.blog.service.IBlogSettingService;
import com.blog.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 博客设置表 服务实现类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Service
@Slf4j
public class BlogSettingServiceImpl extends ServiceImpl<BlogSettingMapper, BlogSetting> implements IBlogSettingService {

    /**
     * 获取博客详情信息
     *
     * @return
     */
    @Override
    @Cacheable(value = RedisConstants.BLOG_SETTING_CACHE, unless = "#result == null")
    public BlogSetting getBlogSettingDetail() {
        //1.查询博客信息
        BlogSetting blogSetting = this.getOne(Wrappers.<BlogSetting>lambdaQuery().last("LIMIT 1"));
        //判断
        if (blogSetting == null) {
            log.error("博客不存在");
        }
        //2.返回结果
        return blogSetting;
    }


    /**
     * 更新博客信息
     *
     * @param blogSettingDTO
     * @return
     */
    @Override
    @CacheEvict(value = RedisConstants.BLOG_SETTING_CACHE, allEntries = true)
    public void updateBlogSetting(BlogSettingDTO blogSettingDTO) {
        //1.获取现有博客记录
        BlogSetting existingBlot = this.getOne(Wrappers.<BlogSetting>lambdaQuery().last("LIMIT 1"));
        if (existingBlot == null) {
            BlogSetting newBlog = new BlogSetting();
            BeanUtils.copyProperties(blogSettingDTO, newBlog);
            this.saveOrUpdate(newBlog);
            return;
        }

        LambdaUpdateWrapper<BlogSetting> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BlogSetting::getId, existingBlot.getId());
        if (blogSettingDTO.getBlogName() !=null)
            wrapper.set(BlogSetting::getBlogName, blogSettingDTO.getBlogName());
        if (blogSettingDTO.getAuthor() != null)
            wrapper.set(BlogSetting::getAuthor, blogSettingDTO.getAuthor());
        if (blogSettingDTO.getAvatar() != null)
            wrapper.set(BlogSetting::getAvatar, blogSettingDTO.getAvatar());
        if (blogSettingDTO.getIntroduction() != null)
            wrapper.set(BlogSetting::getIntroduction, blogSettingDTO.getIntroduction());
        if (blogSettingDTO.getGiteeHome() != null)
            wrapper.set(BlogSetting::getCsdnHome, blogSettingDTO.getCsdnHome());
        if (blogSettingDTO.getGiteeHome() != null)
            wrapper.set(BlogSetting::getGiteeHome, blogSettingDTO.getGiteeHome());
        if (blogSettingDTO.getGithubHome() != null)
            wrapper.set(BlogSetting::getGithubHome, blogSettingDTO.getGithubHome());
        if (blogSettingDTO.getZhihuHome() != null)
            wrapper.set(BlogSetting::getZhihuHome, blogSettingDTO.getZhihuHome());

        if (!this.update(wrapper)) {
            throw new BusinessException("博客更新失败！");
        }
    }
}

