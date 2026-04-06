package com.blog.controller.admin;

import com.blog.dto.LoginDTO;
import com.blog.result.Result;
import com.blog.service.IAuthService;
import com.blog.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "认证管理", description = "登录、认证接口")
@RequiredArgsConstructor
@RestController
public class AdminAuthController {

    private final IAuthService authService;


    /**
     * 用于开发测试
     * @return
     */
    @GetMapping("/login")
    public Map<String, String> loginPage() {
        Map<String, String> map = new HashMap<>();
        map.put("msg", "这是登录页面，请使用 POST 方法提交登录信息");
        map.put("status", "success");
        return map;
    }

    /**
     * 登录认证
     *
     * @param request
     * @return
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO request) {
        LoginVO response = authService.login(request);
        return Result.ok(response);
    }
}