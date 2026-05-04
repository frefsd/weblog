package com.blog.controller.admin;

import com.blog.result.Result;
import com.blog.service.IDashboardService;
import com.blog.vo.CategoryArticleCountVO;
import com.blog.vo.DashboardArticleStatisticsVO;
import com.blog.vo.TagArticleCountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "统计相关接口", description = "统计各类数据")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/dashboard")
public class AdminDashBoardController {

    private final IDashboardService dashboardService;

    @Operation(summary = "获取文章统计信息")
    @PostMapping("/article/statistics")
    public Result<DashboardArticleStatisticsVO> getDashboardArticleStatisticsInfo() {
        return Result.ok(dashboardService.getArticleStatistics());
    }

    @Operation(summary = "获取发布文章统计信息（最近 7 天）")
    @PostMapping("/publishArticle/statistics")
    public Result<List<Map<String, Object>>> getDashboardPublishArticleStatisticsInfo() {
        return Result.ok(dashboardService.getPublishArticleStatistics());
    }

    @Operation(summary = "获取 PV 统计信息（最近 7 天）")
    @PostMapping("/pv/statistics")
    public Result<List<Map<String, Object>>> getDashboardPVStatisticsInfo() {
        return Result.ok(dashboardService.getPVStatistics());
    }

    @Operation(summary = "获取文章分类统计（扇形图）")
    @PostMapping("/category/statistics")
    public Result<List<CategoryArticleCountVO>> getCategoryArticleStatistics() {
        return Result.ok(dashboardService.getCategoryArticleStatistics());
    }

    @Operation(summary = "获取文章标签统计（柱状图）")
    @PostMapping("/tag/statistics")
    public Result<List<TagArticleCountVO>> getTagArticleStatistics() {
        return Result.ok(dashboardService.getTagArticleStatistics());
    }
}
