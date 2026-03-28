package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.UpdatePasswordDTO;
import com.blog.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
public interface IUserService extends IService<User> {


    /**
     * 获取用户详情信息
     *
     * @return
     */
    User getCurrentUser();

    /**
     * 修改当前登录用户密码
     *
     * @param request
     */
    void updateCurrentUserPassword(UpdatePasswordDTO request);

    /**
     * 头像上传
     * @param file
     * @return
     */
    Map<String, String> uploadAvatar(MultipartFile file);
}
