package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.*;
import com.blog.entity.Article;
import com.blog.vo.ArchiveItemVO;
import com.blog.vo.ArticleDetailVO;
import com.blog.vo.ArticleFrontendDetailVO;
import com.blog.vo.ArticleIndexVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * <p>
 * 文章表 服务类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
public interface IArticleService extends IService<Article> {

    /**
     * 获取文章详情
     */
    ArticleDetailVO getArticleDetailForAdmin(ArticleDetailDTO query);

    /**
     * 文章分页查询
     */
    IPage<Article> getArticlePageList(ArticlePageDTO query);

    /**
     * 发布文章
     */
    void publishArticle(ArticlePublishDTO dto);

    /**
     * 修改文章
     */
    void updateArticle(ArticleUpdateDTO dto);

    /**
     * 删除文章
     */
    void deleteArticle(ArticleDeleteDTO dto);

    /**
     * 获取前台首页文章分页列表
     */
    IPage<ArticleIndexVO> getArticleIndexPage(ArticlePageDTO query);

    /**
     * 获取前台文章详情
     */
    ArticleFrontendDetailVO getArticleDetailForFrontend(Long articleId, HttpServletRequest request);

    /**
     * 获取缓存的文章详情（仅内部调用，数据不包含最新阅读数）
     */
    ArticleFrontendDetailVO getCachedArticleDetail(Long articleId);

    /**
     * 根据分类 ID 获取文章列表
     */
    IPage<ArticleIndexVO> getArticlesByCategoryId(Long categoryId, ArticlePageDTO query);

    /**
     * 根据标签 ID 获取文章列表
     */
    IPage<ArticleIndexVO> getArticlesByTagId(Long tagId, ArticlePageDTO query);

    /**
     * 获取归档列表
     */
    List<ArchiveItemVO> getArchiveList();

    /**
     * 搜索文章（前台）
     */
    IPage<ArticleIndexVO> searchArticles(ArticleSearchDTO dto);
}
