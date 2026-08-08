package com.blog.controller.frontend;

import com.blog.result.Result;
import com.blog.service.IGameService;
import com.blog.utils.IpUtil;
import com.blog.utils.RateLimiter;
import com.blog.vo.GameReplyVO;
import com.blog.vo.GameStartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "前台游戏接口", description = "哄女友小游戏")
@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {

    /** 游戏限流：每 IP 每分钟最多 10 次（调用 DeepSeek API，防止刷爆付费额度） */
    private static final int GAME_MAX_PER_MINUTE = 10;

    private final IGameService gameService;
    private final RateLimiter rateLimiter;

    @Operation(summary = "开始游戏")
    @PostMapping("/start")
    public Result<GameStartVO> startGame(HttpServletRequest servletRequest) {
        if (!rateLimiter.tryAcquire("game:start:" + IpUtil.getClientIp(servletRequest), GAME_MAX_PER_MINUTE, 60_000)) {
            return Result.fail("请求过于频繁，请稍后再试");
        }
        return Result.ok(gameService.startGame());
    }

    @Operation(summary = "发送回复")
    @PostMapping("/reply")
    public Result<GameReplyVO> sendReply(@RequestBody Map<String, String> body, HttpServletRequest servletRequest) {
        if (!rateLimiter.tryAcquire("game:reply:" + IpUtil.getClientIp(servletRequest), GAME_MAX_PER_MINUTE, 60_000)) {
            return Result.fail("请求过于频繁，请稍后再试");
        }
        return Result.ok(gameService.processReply(body.get("sessionId"), body.get("content")));
    }

    @Operation(summary = "关闭游戏")
    @PostMapping("/close")
    public Result<Void> closeGame(@RequestBody Map<String, String> body) {
        gameService.closeGame(body.get("sessionId"));
        return Result.ok();
    }
}
