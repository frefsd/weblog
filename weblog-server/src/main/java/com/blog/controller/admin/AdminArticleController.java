package com.blog.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.dto.*;
import com.blog.entity.Article;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.IArticleService;
import com.blog.vo.ArticleDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "文章相关接口", description = "用于文章的修改和查看详情")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/article")
public class AdminArticleController {

    //依赖注入
    private final IArticleService articleService;

    /**
     * 获取文章详情
     */
    @Operation(summary = "获取文章详情")
    @PostMapping("/detail")
    public Result<ArticleDetailVO> getArticleDetailForAdmin(@Valid @RequestBody ArticleDetailDTO query) {
        ArticleDetailVO detail = articleService.getArticleDetailForAdmin(query);
        return Result.ok(detail);
    }

    /**
     * 发布文章
     *
     * @param dto
     * @return
     */
    @Operation(summary = "发布文章")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/publish")
    public Result<Void> publishArticle(@Valid @RequestBody ArticlePublishDTO dto) {
        articleService.publishArticle(dto);
        return Result.ok();
    }

    /**
     * 文章分页查询
     *
     * @param query
     * @return
     */
    @Operation(summary = "分页查询")
    @PostMapping("/list")
    public Result<PageResult<Article>> getArticlePageList(@Valid @RequestBody ArticlePageDTO query) {
        IPage<Article> page = articleService.getArticlePageList(query);
        return Result.page(page);
    }

    /**
     * 删除文章
     *
     * @param dto
     * @return
     */
    @Operation(summary = "删除文章")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete")
    public Result<Void> deleteArticle(@Valid @RequestBody ArticleDeleteDTO dto) {
        articleService.deleteArticle(dto);
        return Result.ok();
    }

    /**
     * 修改文章详情
     *
     * @param dto
     * @return
     */
    @Operation(summary = "修改文章详情")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update")
    public Result<Void> updateArticle(@Valid @RequestBody ArticleUpdateDTO dto) {
        articleService.updateArticle(dto);
        return Result.ok();
    }
}
