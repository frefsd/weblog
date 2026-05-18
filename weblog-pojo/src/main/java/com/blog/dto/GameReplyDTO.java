package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GameReplyDTO {
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @NotBlank(message = "回复内容不能为空")
    private String content;
}
