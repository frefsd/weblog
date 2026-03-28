package com.blog.controller.admin;

import com.blog.dto.BlogSettingDTO;
import com.blog.entity.BlogSetting;
import com.blog.result.Result;
import com.blog.service.IBlogSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminBlogSettingController")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "博客管理", description = "查看和修改博客详情")
@RequestMapping("/admin/blog/setting")
public class AdminBlogSettingController {
    private final IBlogSettingService blogSettingService;

    /**
     * 获取博客详情信息
     * @return
     */
    @Operation(summary = "获取博客详情")
    @PostMapping("/detail")
    public Result<BlogSetting> getBlogSettingDetail(){
       BlogSetting blogSetting = blogSettingService.getBlogSettingDetail();
        return Result.ok(blogSetting);
    }

    /**
     * 更新博客信息
     * @param blogSettingDTO
     * @return
     */
    @Operation(summary = "更新博客信息")
    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateBlogSetting(@Valid @RequestBody BlogSettingDTO blogSettingDTO){
        blogSettingService.updateBlogSetting(blogSettingDTO);
        return Result.ok();
    }
}
