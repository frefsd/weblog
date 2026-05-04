package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.ArticleContent;
import com.blog.mapper.ArticleContentMapper;
import com.blog.service.IArticleContentService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文章内容表 服务实现类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Service
public class ArticleContentServiceImpl extends ServiceImpl<ArticleContentMapper, ArticleContent> implements IArticleContentService {

}
