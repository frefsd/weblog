package com.blog.service.impl;

import com.blog.dto.LoginDTO;
import com.blog.exception.BusinessException;
import com.blog.service.IAuthService;
import com.blog.utils.JwtUtil;
import com.blog.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * 登录认证
     *
     * @param request
     * @return
     */
    @Override
    public LoginVO login(LoginDTO request) {
        try {
            // 1. 执行 Spring Security 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            // 2. 认证成功，生成 Token
            String token = jwtUtil.generateToken(request.getUsername());

            return new LoginVO(token);

        } catch (BadCredentialsException e) {
            log.warn("登录失败：用户名或密码错误 - {}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        } catch (Exception e) {
            log.error("登录发生未知异常", e);
            throw new BusinessException("系统繁忙，请稍后重试");
        }
    }

}
