package com.blog.service;

import com.blog.vo.GameReplyVO;
import com.blog.vo.GameStartVO;

public interface IGameService {

    GameStartVO startGame();

    GameReplyVO processReply(String sessionId, String content);

    void closeGame(String sessionId);
}
