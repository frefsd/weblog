package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.dto.TagAddDTO;
import com.blog.dto.TagDeleteDTO;
import com.blog.dto.TagPageDTO;
import com.blog.dto.TagSearchDTO;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.ITagService;
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

@Tag(name = "文章标签相关接口", description = "管理标签的相关信息")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/tag")
public class AdminTagController {

    private final ITagService tagService;

    /**
     * 新增标签
     *
     * @param dto
     * @return
     */
    @Operation(summary = "新增标签")
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> addTags(@Valid @RequestBody TagAddDTO dto) {
        tagService.addTags(dto);
        return Result.ok();
    }

    /**
     * 分页查询标签
     *
     * @param dto
     * @return
     */
    @Operation(summary = "分页查询标签")
    @PostMapping("/list")
    public Result<PageResult<com.blog.entity.Tag>> pageTags(@Valid @RequestBody TagPageDTO dto) {
        IPage<com.blog.entity.Tag> page = tagService.pageTags(dto);
        return Result.page(page);
    }

    /**
     * 删除标签
     *
     * @param dto
     * @return
     */
    @Operation(summary = "删除标签")
    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteTag(@Valid @RequestBody TagDeleteDTO dto) {
        tagService.deleteTag(dto);
        return Result.ok();
    }

    /**
     * 标签下拉列表
     *
     * @return
     */
    @Operation(summary = "标签下拉列表")
    @PostMapping("/select/list")
    public Result<List<SelectOptionVO>> selectList() {
        return Result.ok(tagService.selectList());
    }

    /**
     * 标签搜索
     *
     * @param dto
     * @return
     */
    @Operation(summary = "标签搜索")
    @PostMapping("/search")
    public Result<List<SelectOptionVO>> search(@RequestBody TagSearchDTO dto) {
        return Result.ok(tagService.search(dto));
    }
}
