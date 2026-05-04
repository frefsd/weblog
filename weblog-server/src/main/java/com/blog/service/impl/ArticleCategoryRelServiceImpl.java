package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.ArticleCategoryRel;
import com.blog.mapper.ArticleCategoryRelMapper;
import com.blog.service.IArticleCategoryRelService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文章所属分类映射表 服务实现类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Service
public class ArticleCategoryRelServiceImpl extends ServiceImpl<ArticleCategoryRelMapper, ArticleCategoryRel> implements IArticleCategoryRelService {

}
