package com.blog.controller.frontend;

import com.blog.dto.ArticlePageDTO;
import com.blog.result.Result;
import com.blog.service.IArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "前台首页接口", description = "用于前台首页文章列表查询")
@RestController
@RequiredArgsConstructor
@RequestMapping("/index")
public class IndexController {

    private final IArticleService articleService;

    @Operation(summary = "获取首页文章列表")
    @PostMapping("/article/list")
    public Result getArticleList(@Valid @RequestBody ArticlePageDTO dto) {
        return Result.ok(articleService.getArticleIndexPage(dto));
    }
}
