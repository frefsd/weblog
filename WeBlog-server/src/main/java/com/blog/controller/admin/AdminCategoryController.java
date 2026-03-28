package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.dto.CategoryAddDTO;
import com.blog.dto.CategoryDeleteDTO;
import com.blog.dto.CategoryPageDTO;
import com.blog.dto.CategorySearchDTO;
import com.blog.entity.Category;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.ICategoryService;
import com.blog.vo.SelectOptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "文章分类接口", description = "用于文章的修改和发布")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/category")
public class AdminCategoryController {
    private final ICategoryService categoryService;

    /**
     * 添加分类
     * @param dto
     * @return
     */
    @Operation(summary = "添加分类")
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> addCategory(@Valid @RequestBody CategoryAddDTO dto){
        categoryService.addCategory(dto);
        return Result.ok();
    }

    /**
     * 分页查询文章分类
     * @param dto
     * @return
     */
    @Operation(summary = "分页查询文章分类")
    @PostMapping("/list")
    public Result<PageResult<Category>> getCategoryList(@Valid @RequestBody CategoryPageDTO dto){
        IPage<Category> page = categoryService.pageCategories(dto);
        return Result.page(page);
    }

    /**
     * 删除分类
     * @param dto
     * @return
     */
    @Operation(summary = "删除分类")
    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCategory(@Valid @RequestBody CategoryDeleteDTO dto){
        categoryService.deleteCategory(dto);
        return Result.ok();
    }

    /**
     * 获取分类列表
     * @return
     */
    @Operation(summary = "获取分类列表")
    @PostMapping("/select/list")
    public Result<List<SelectOptionVO>> getCategorySelect(){
        return Result.ok(categoryService.selectList());
    }

    /**
     * 分类搜索
     * @param dto
     * @return
     */
    @Operation(summary = "分类搜索")
    @PostMapping("/search")
    public Result<List<SelectOptionVO>> searchCategory(@RequestBody CategorySearchDTO dto){
        return Result.ok(categoryService.search(dto));
    }
}
