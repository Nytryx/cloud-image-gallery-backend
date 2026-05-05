package com.nytryx.gallery.execption;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessExecption extends RuntimeException{

    /**
     * 异常错误码
     */
    private final int code;

    public BusinessExecption(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessExecption(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessExecption(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
