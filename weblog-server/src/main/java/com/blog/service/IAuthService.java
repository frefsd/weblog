package com.blog.service;

import com.blog.dto.LoginDTO;
import com.blog.vo.LoginVO;

public interface IAuthService {
    /**
     * 执行登录认证
     *
     * @param request
     * @return
     */
    LoginVO login(LoginDTO request);
}
