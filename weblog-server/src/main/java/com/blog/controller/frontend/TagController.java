package com.blog.controller.frontend;

import com.blog.dto.TagArticleDTO;
import com.blog.result.Result;
import com.blog.service.IArticleService;
import com.blog.service.ITagService;
import com.blog.vo.TagSimpleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "前台标签接口", description = "用于前台标签相关查询")
@RestController
@RequiredArgsConstructor
@RequestMapping("/tag")
public class TagController {

    private final ITagService tagService;
    private final IArticleService articleService;

    @Operation(summary = "获取全部标签")
    @PostMapping("/list")
    public Result<List<TagSimpleVO>> getTags() {
        return Result.ok(tagService.getAllTags());
    }

    @Operation(summary = "获取标签下的文章列表")
    @PostMapping("/article/list")
    public Result getArticleList(@Valid @RequestBody TagArticleDTO dto) {
        return Result.ok(articleService.getArticlesByTagId(dto.getTagId(), dto));
    }
}
