package com.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameReplyVO {
    private String reply;
    private String emotion;
    private int forgiveness;
    private int scoreChange;
    private String status; // playing | won | lost
}
