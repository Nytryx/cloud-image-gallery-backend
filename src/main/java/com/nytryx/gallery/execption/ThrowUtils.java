package com.nytryx.gallery.execption;

/**
 * 业务异常处理工具类
 */
public class ThrowUtils {

    /**
     * 如果条件成立则抛出异常
     * @param condition 条件
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 如果条件成立则抛出异常
     * @param condition 条件
     * @param errorCode 自定义错误码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
       throwIf(condition, new BusinessExecption(errorCode));
    }

    /**
     * 如果条件成立则抛出异常
     * @param condition 条件
     * @param errorCode 自定义错误码
     * @param message 错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessExecption(errorCode, message));
    }

}
