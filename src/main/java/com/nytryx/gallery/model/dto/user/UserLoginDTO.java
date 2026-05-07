package com.nytryx.gallery.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求类
 */
@Data
public class UserLoginDTO implements Serializable {
    private static final long serialVersionUID = 506029340944529135L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

}
