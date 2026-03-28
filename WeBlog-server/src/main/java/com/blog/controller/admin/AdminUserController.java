package com.blog.controller.admin;

import com.blog.dto.UpdatePasswordDTO;
import com.blog.entity.User;
import com.blog.result.Result;
import com.blog.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "用户管理", description = "用户相关接口")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminUserController {

    private final IUserService userService;

    /**
     * 获取用户详情信息
     *
     * @return
     */
    @Operation(summary = "获取用户详情信息")
    @PostMapping("/detail")
    public Result<User> getAdminInfo() {
        User user = userService.getCurrentUser();
        return Result.ok(user);
    }

    /**
     * 修改当前登录用户密码
     */
    @Operation(summary = "修改用户密码")
    @PostMapping("/password/update")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateAdminPassword(@Valid @RequestBody UpdatePasswordDTO request) {
        userService.updateCurrentUserPassword(request);
        return Result.ok();
    }

    /**
     * 头像上传
     *
     * @param file
     * @return
     */
    @Operation(summary = "文件上传")
    @PostMapping("/file/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, String>> updateFile(@RequestParam(name = "file") MultipartFile file) {
            // 调用Service层处理文件上传
            Map<String, String> result = userService.uploadAvatar(file);
            return Result.ok(result);
    }
}
