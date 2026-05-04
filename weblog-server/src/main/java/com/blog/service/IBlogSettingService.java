package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.BlogSettingDTO;
import com.blog.entity.BlogSetting;

/**
 * <p>
 * 博客设置表 服务类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
public interface IBlogSettingService extends IService<BlogSetting> {

    /**
     * 获取博客详情信息
     * @return
     */
    BlogSetting getBlogSettingDetail();


    /**
     * 更新博客信息
     * @param blogSettingDTO
     * @return
     */
    void updateBlogSetting(BlogSettingDTO blogSettingDTO);

}
