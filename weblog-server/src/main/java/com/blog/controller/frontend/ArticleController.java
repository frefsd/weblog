package com.blog.controller.frontend;

import com.blog.dto.ArticleDetailDTO;
import com.blog.dto.ArticleSearchDTO;
import com.blog.result.Result;
import com.blog.service.IArticleService;
import com.blog.vo.ArticleFrontendDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "前台文章接口", description = "用于前台文章详情查询")
@RestController
@RequiredArgsConstructor
@RequestMapping("/article")
public class ArticleController {

    private final IArticleService articleService;

    @Operation(summary = "获取文章详情")
    @PostMapping("/detail")
    public Result<ArticleFrontendDetailVO> getArticleDetail(@Valid @RequestBody ArticleDetailDTO dto) {
        return Result.ok(articleService.getArticleDetailForFrontend(dto.getArticleId()));
    }

    @Operation(summary = "搜索文章")
    @PostMapping("/search")
    public Result searchArticles(@Valid @RequestBody ArticleSearchDTO dto) {
        return Result.page(articleService.searchArticles(dto));
    }
}
