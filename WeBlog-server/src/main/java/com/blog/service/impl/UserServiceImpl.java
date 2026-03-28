package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.dto.UpdatePasswordDTO;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.service.IUserService;
import com.blog.utils.AliyunOSSOperator;
import com.blog.utils.AliyunOSSProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final PasswordEncoder passwordEncoder;
    private final AliyunOSSOperator aliyunOSSOperator;
    private final AliyunOSSProperties aliyunOSSProperties;

    /**
     * 获取当前登录用户详情
     * @return
     */
    @Override
    public User getCurrentUser() {
        // 1. 获取当前认证信息
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException("用户未登录");
        }

        String username = authentication.getName();

        // 2. 查询用户
        User user = this.getOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0));

        if (user == null) {
            throw new BusinessException("用户不存在或已被删除");
        }
        return user;
    }

    /**
     * 修改当前登录用户密码
     * @param request
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 加上事务注解
    public void updateCurrentUserPassword(UpdatePasswordDTO request) {
        // 1. 获取当前用户
        User currentUser = getCurrentUser();
        User realUser = this.getById(currentUser.getId());
        if (realUser == null) {
            throw new BusinessException("用户数据异常");
        }

        // 2. 业务逻辑：防止新密码与旧密码相同
        if (passwordEncoder.matches(request.getNewPassword(), realUser.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 3. 加密并更新
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        realUser.setPassword(encodedPassword);

        boolean success = this.updateById(realUser);
        if (!success) {
            throw new BusinessException("密码更新失败！");
        }
    }

    /**
     * 头像上传
     * @param file
     * @return
     */
    @Override
    public Map<String, String> uploadAvatar(MultipartFile file) {
        log.info("上传用户头像：文件名={}, 大小={}字节, 类型={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        // 1. 验证文件是否为空
        if (file.isEmpty()) {
            log.warn("上传文件为空");
            throw new BusinessException("上传文件不能为空");
        }

        // 2. 验证文件类型
        List<String> allowTypes = aliyunOSSProperties.getAllowTypes();
        if (allowTypes != null && !allowTypes.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            boolean validExtension = false;

            // 首先检查文件扩展名
            if (originalFilename != null && originalFilename.contains(".")) {
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
                if (allowTypes.contains(fileExtension)) {
                    validExtension = true;
                }
            }

            // 如果扩展名无效，检查Content-Type
            if (!validExtension) {
                String contentType = file.getContentType();
                boolean validContentType = false;

                if (contentType != null) {
                    // 检查Content-Type是否匹配允许的类型
                    if (contentType.equals("image/jpeg") && (allowTypes.contains("jpg") || allowTypes.contains("jpeg"))) {
                        validContentType = true;
                    } else if (contentType.equals("image/jpg") && (allowTypes.contains("jpg") || allowTypes.contains("jpeg"))) {
                        validContentType = true;
                    } else if (contentType.equals("image/png") && allowTypes.contains("png")) {
                        validContentType = true;
                    } else if (contentType.startsWith("image/")) {
                        // 对于其他图片类型，检查扩展名是否在允许列表中
                        log.warn("Content-Type不允许: {}, 允许的类型: {}", contentType, allowTypes);
                        throw new BusinessException("只允许上传 " + String.join(", ", allowTypes) + " 格式的图片");
                    }
                }

                if (!validContentType) {
                    log.warn("文件类型不允许: 文件名={}, Content-Type={}, 允许的类型: {}",
                            originalFilename, contentType, allowTypes);
                    throw new BusinessException("只允许上传 " + String.join(", ", allowTypes) + " 格式的文件");
                }
            }
        }

        try {
            // 3. 上传文件（传递原始文件名，由AliyunOSSOperator内部处理文件名生成）
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                // 如果文件名为空，生成一个默认文件名
                String contentType = file.getContentType();
                if (contentType != null && contentType.contains("jpeg")) {
                    originalFilename = "uploaded_file.jpg";
                } else if (contentType != null && contentType.contains("png")) {
                    originalFilename = "uploaded_file.png";
                } else {
                    originalFilename = "uploaded_file";
                }
            }
            String url = aliyunOSSOperator.upload(file.getBytes(), originalFilename);

            log.info("用户头像上传成功: URL={}", url);

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return result;
        } catch (Exception e) {
            log.error("用户头像上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
}