package com.nytryx.gallery.execption;

import com.nytryx.gallery.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice // 环绕切面注解，允许在全局异常处理器中自定义一些切点（注意：这不是AOP）
@Slf4j
public class GlobalExecptionHandler {

    @ExceptionHandler(BusinessExecption.class)
    public Result<?> businessExecptionHandler(BusinessExecption execption) {
        log.error("BusinessExecption: ", execption);
        return Result.error(execption.getCode(), execption.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> businessExecptionHandler(RuntimeException execption) {
        log.error("RuntimeException: ", execption);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
    }
}
