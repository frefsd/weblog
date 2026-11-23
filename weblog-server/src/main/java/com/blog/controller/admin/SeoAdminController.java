package com.blog.controller.admin;

import com.blog.result.Result;
import com.blog.service.SeoRebuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * SEO 重建管理接口。
 */
@Tag(name = "SEO 管理", description = "前端站点预渲染重建")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/seo")
public class SeoAdminController {

    private final SeoRebuildService seoRebuildService;

    @Operation(summary = "手动触发站点重建")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rebuild")
    public Result<String> rebuild() {
        // 直接回显真实结果，避免"未启用/未配置"时给假成功
        return Result.ok(seoRebuildService.schedule("手动触发（管理端）"));
    }

    @Operation(summary = "查询重建状态")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/status")
    public Result<Map<String, Object>> status() {
        return Result.ok(seoRebuildService.status());
    }
}
