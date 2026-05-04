package com.blog.controller.frontend;

import com.blog.result.Result;
import com.blog.service.IArticleService;
import com.blog.vo.ArchiveItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "前台归档接口", description = "用于前台归档列表查询")
@RestController
@RequiredArgsConstructor
@RequestMapping("/archive")
public class ArchiveController {

    private final IArticleService articleService;

    @Operation(summary = "获取归档列表")
    @PostMapping("/list")
    public Result<List<ArchiveItemVO>> getArchive() {
        List<ArchiveItemVO> archiveList = articleService.getArchiveList();
        return Result.ok(archiveList);
    }
}
