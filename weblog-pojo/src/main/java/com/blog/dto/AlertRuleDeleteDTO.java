package com.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertRuleDeleteDTO {
    @NotNull(message = "ruleId不能为空")
    private Long id;
}
