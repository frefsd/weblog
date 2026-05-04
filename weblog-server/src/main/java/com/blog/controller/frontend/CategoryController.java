package com.blog.controller.frontend;

import com.blog.dto.CategoryArticleDTO;
import com.blog.result.Result;
import com.blog.service.IArticleService;
import com.blog.service.ICategoryService;
import com.blog.vo.CategorySimpleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "前台分类接口", description = "用于前台分类相关查询")
@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final ICategoryService categoryService;
    private final IArticleService articleService;

    @Operation(summary = "获取全部分类")
    @PostMapping("/list")
    public Result<List<CategorySimpleVO>> getCategories() {
        return Result.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "获取分类下的文章列表")
    @PostMapping("/article/list")
    public Result getArticleList(@Valid @RequestBody CategoryArticleDTO dto) {
        return Result.ok(articleService.getArticlesByCategoryId(dto.getCategoryId(), dto));
    }
}
