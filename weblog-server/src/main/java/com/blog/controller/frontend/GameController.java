package com.blog.controller.frontend;

import com.blog.result.Result;
import com.blog.service.IGameService;
import com.blog.vo.GameReplyVO;
import com.blog.vo.GameStartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "前台游戏接口", description = "哄女友小游戏")
@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {

    private final IGameService gameService;

    @Operation(summary = "开始游戏")
    @PostMapping("/start")
    public Result<GameStartVO> startGame() {
        return Result.ok(gameService.startGame());
    }

    @Operation(summary = "发送回复")
    @PostMapping("/reply")
    public Result<GameReplyVO> sendReply(@RequestBody Map<String, String> body) {
        return Result.ok(gameService.processReply(body.get("sessionId"), body.get("content")));
    }

    @Operation(summary = "关闭游戏")
    @PostMapping("/close")
    public Result<Void> closeGame(@RequestBody Map<String, String> body) {
        gameService.closeGame(body.get("sessionId"));
        return Result.ok();
    }
}
