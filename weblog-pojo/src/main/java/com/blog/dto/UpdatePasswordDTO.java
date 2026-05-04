package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordDTO {
    /**
     * 设置新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "新密码长度至少为6位")
    private String newPassword;

    /**
     * 再次确认新密码
     */
    private String rePassword;
}
