package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.ArticleTagRel;
import com.blog.mapper.ArticleTagRelMapper;
import com.blog.service.IArticleTagRelService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文章对应标签映射表 服务实现类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Service
public class ArticleTagRelServiceImpl extends ServiceImpl<ArticleTagRelMapper, ArticleTagRel> implements IArticleTagRelService {

}
