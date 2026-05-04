package com.blog.controller.frontend;

import com.blog.entity.BlogSetting;
import com.blog.result.Result;
import com.blog.service.IBlogSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "博客前台接口", description = "用于前台展示博客信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/blog/setting")
public class BlogSettingController {

    private final IBlogSettingService blogSettingService;


    /**
     * 获取博客详情（前台展示）
     * @return
     */
    @Operation(summary = "获取博客详情（前台展示）")
    @PostMapping("/detail")
    public Result<BlogSetting> getBlogSettingDetail(){
        BlogSetting blogSettingDetail = blogSettingService.getBlogSettingDetail();
        return Result.ok(blogSettingDetail);
    }
}
