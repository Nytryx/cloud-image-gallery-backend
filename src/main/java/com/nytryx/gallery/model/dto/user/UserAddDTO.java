package com.nytryx.gallery.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求类 （管理员使用）
 */
@Data
public class UserAddDTO implements Serializable {

    private static final long serialVersionUID = -8084893255981272396L;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色
     */
    private String userRole;
}
