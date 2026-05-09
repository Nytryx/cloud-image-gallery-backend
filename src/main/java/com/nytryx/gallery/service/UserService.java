package com.nytryx.gallery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nytryx.gallery.model.dto.user.UserQueryDTO;
import com.nytryx.gallery.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nytryx.gallery.model.vo.UserLoginVO;
import com.nytryx.gallery.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务接口
 * @author zhent
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request 当前 HTTP 请求的“封装对象”
     * @return 脱敏后的用户数据
     */
    UserLoginVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销
     * @param request 当前 HTTP 请求的“封装对象”
     * @return 是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前的登录用户
     * @param request 当前 HTTP 请求的“封装对象”
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获得脱敏后的登录用户信息
     * @param user 数据库查询得到的用户完整信息
     * @return 脱敏后的用户登录信息
     */
    UserLoginVO getLoginUserVO(User user);

    /**
     * 获取脱敏后的用户信息
     * @param user 数据库查询得到的用户完整信息
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     * @param userList 数据库查询得到的用户完整信息列表
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 密码加密方法
     * @param password 原密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String password);

    /**
     * 获取MyBatis-Plus查询包装器
     * @param userQueryDTO 用户查询请求对象
     * @return MyBatis Plus查询包装器
     */
    QueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryDTO);

    /**
     * 判断用户是否为管理员
     *
     * @param user 用户
     * @return 是或不是
     */
    boolean isAdmin(User user);
}
