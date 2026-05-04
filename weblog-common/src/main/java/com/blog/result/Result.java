package com.blog.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {
    private Boolean success; // 成功 true 失败 false
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    /**
     * 分页统一返回
     */
    public static <T> Result<PageResult<T>> page(IPage<T> page) {
        if (page == null) {
            return ok(PageResult.of(null, 0L, 1L, 10L, 0L));
        }
        PageResult<T> data = PageResult.of(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
        return ok(data);
    }
}

