package com.blog.exception;

import com.blog.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 统一异常处理类
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 数据重复插入捕获
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleSQLIntegrityConstraintViolationException(DuplicateKeyException e) {
        String message = e.getMessage();
        log.warn("数据库唯一键冲突: {}", message);

        // 正则匹配：Duplicate entry 'xxx' for key 'yyy'
        Pattern pattern = Pattern.compile("Duplicate entry '(.*?)' for key '(.*?)'");
        Matcher matcher = pattern.matcher(message);

        // 如果正则没匹配上（兼容老版本或其他数据库）
        return Result.fail("操作失败：数据已存在");
    }

    //JSR303参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 获取第一个错误信息（也可返回全部）
        String errorMsg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");

        return Result.fail(errorMsg);
    }

    /**
     * 处理 Spring Security 的“权限不足”异常
     * 当 test 用户尝试调用 @PreAuthorize("hasRole('ADMIN')") 保护的接口时，会触发这里
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 返回 HTTP 403 状态码
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        // 返回统一的失败结果
        return Result.fail("权限不足：您没有执行该操作的权限!");
    }

    /**
     * 处理业务异常（你抛的 BusinessException）
     */
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e) {
        return Result.fail(e.getMessage());
    }

}