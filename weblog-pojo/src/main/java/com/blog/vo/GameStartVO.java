package com.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameStartVO {
    private String sessionId;
    private String scenario;
    private int forgiveness;
}
