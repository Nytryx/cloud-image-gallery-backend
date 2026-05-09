package com.nytryx.gallery.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nytryx.gallery.annotation.AuthCheck;
import com.nytryx.gallery.common.DeleteRequest;
import com.nytryx.gallery.common.Result;
import com.nytryx.gallery.constant.UserConstant;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.execption.ThrowUtils;
import com.nytryx.gallery.model.dto.user.*;
import com.nytryx.gallery.model.entity.User;
import com.nytryx.gallery.model.vo.UserLoginVO;
import com.nytryx.gallery.model.vo.UserVO;
import com.nytryx.gallery.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户接口
 * @author zhent
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Long> userRegisiter(@RequestBody UserRegisterDTO userRegisterDTO) {
        ThrowUtils.throwIf(userRegisterDTO == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterDTO.getUserAccount();
        String userPassword = userRegisterDTO.getUserPassword();
        String checkPassword = userRegisterDTO.getCheckPassword();
        long userId = userService.userRegister(userAccount, userPassword, checkPassword);
        return Result.success(userId);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserLoginVO> userLogin(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginDTO == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginDTO.getUserAccount();
        String userPassword = userLoginDTO.getUserPassword();
        UserLoginVO userLoginVO = userService.userLogin(userAccount, userPassword, request);
        return Result.success(userLoginVO);
    }

    /**
     * 获取当前的登录用户
     */
    @GetMapping("/get/login")
    public Result<UserLoginVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return Result.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public Result<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        return Result.success(userService.userLogout(request));
    }

    /**
     * 创建用户 （管理员）
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Long> addUser(@RequestBody UserAddDTO userAddDTO) {
        ThrowUtils.throwIf(userAddDTO == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddDTO, user);
        // 设置用户的默认密码
        user.setUserPassword(userService.getEncryptPassword(UserConstant.USER_DEFAULT_PASSWORD));
        // 插入数据库
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return Result.success(user.getId());
    }

    /**
     * 根据id获取用户 （管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<User> getUserById(long userId) {
        ThrowUtils.throwIf(userId <= 0 , ErrorCode.PARAMS_ERROR);
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null , ErrorCode.OPERATION_ERROR);
        return Result.success(user);
    }

    /**
     * 根据id获取用户信息 （需要脱敏）
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public Result<UserVO> getUserVOById(long userId) {
        Result<User> result = getUserById(userId);
        User user = result.getData();
        return Result.success(userService.getUserVO(user));
    }

    /**
     * 根据id删除用户信息 （需要脱敏）
     */
    @GetMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> deleteById(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0 , ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.PARAMS_ERROR);
        return Result.success(true);
    }

    /**
     * 更新用户 （管理员）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> updateUser(@RequestBody UserUpdateDTO userUpdateDTO) {
        ThrowUtils.throwIf(userUpdateDTO == null || userUpdateDTO.getId() == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userUpdateDTO, user);
        // 更新数据
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 分页获取用户封装列表 （管理员）
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Page<UserVO>> listUserVoByPage(@RequestBody UserQueryDTO userQueryDTO) {
        ThrowUtils.throwIf(userQueryDTO == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryDTO.getCurrent();
        long pageSize = userQueryDTO.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryDTO));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return Result.success(userVOPage);
    }

}
