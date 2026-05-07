package com.nytryx.gallery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 表示这个注解用于方法上
@Retention(RetentionPolicy.RUNTIME) // 表示这个注解在运行时生效
public @interface AuthCheck {

    /**
     * 用户必须具有的某个权限，默认为空
     * 这里约定，只要打上注解 AuthCheck，用户就必须登录才能使用
     * 如果希望这个接口用户不登录即可使用，就不加该注解
     */
    String mustRole() default "";
}
