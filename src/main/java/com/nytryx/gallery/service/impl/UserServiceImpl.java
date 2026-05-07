package com.nytryx.gallery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nytryx.gallery.constant.UserConstant;
import com.nytryx.gallery.execption.BusinessExecption;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.execption.ThrowUtils;
import com.nytryx.gallery.model.dto.user.UserQueryDTO;
import com.nytryx.gallery.model.entity.User;
import com.nytryx.gallery.model.enums.UserRoleEnum;
import com.nytryx.gallery.model.vo.UserLoginVO;
import com.nytryx.gallery.model.vo.UserVO;
import com.nytryx.gallery.service.UserService;
import com.nytryx.gallery.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.events.DTD;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
* @author zhent
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    // 因为数据中user_account字段加了唯一索引，因此这里没有加事务
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessExecption(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessExecption(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessExecption(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessExecption(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2.检查用户账号是否已经被注册
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserAccount, userAccount);
        long count = baseMapper.selectCount(lambdaQueryWrapper);
        if (count > 0) {
            throw new BusinessExecption(ErrorCode.PARAMS_ERROR, "用户账号已经被注册");
        }
        // 3.加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        // 4.将数据插入到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("用户");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = save(user);
        if (!saveResult) {
            throw new BusinessExecption(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public UserLoginVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验数据
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户名或者密码错误");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户名或者密码错误");
        // 2.对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3.查询数据库是否存在该用户
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserAccount, userAccount);
        lambdaQueryWrapper.eq(User::getUserPassword, encryptPassword);
        User user = baseMapper.selectOne(lambdaQueryWrapper);
        // 不存在则抛异常
        if (user == null) {
            log.error("user login failed, userAccount doesn't match the userPassword");
            throw new BusinessExecption(ErrorCode.PARAMS_ERROR, "用户名或者密码错误");
        }
        // 4.保存用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return getLoginUserVO(user);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 判断是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null, ErrorCode.OPERATION_ERROR, "未登录");
        // 移除登录态
        request.removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断是否已经登录
        User loginUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        // 再从数据库中查询一遍，因为可能用户修改了部分信息，但是session中的缓存没及时更新
        loginUser = getById(loginUser.getId());
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return loginUser;
    }

    @Override
    public UserLoginVO getLoginUserVO(User user) {
        if (user == null){
            return null;
        }
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtil.copyProperties(user, userLoginVO);
        return userLoginVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollectionUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public String getEncryptPassword(String password) {
        // 加盐，用于混淆密码
        final String SALT = "nytryx";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryDTO) {
        ThrowUtils.throwIf(userQueryDTO == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = userQueryDTO.getId();
        String userName = userQueryDTO.getUserName();
        String userAccount = userQueryDTO.getUserAccount();
        String userProfile = userQueryDTO.getUserProfile();
        String userRole = userQueryDTO.getUserRole();
        String sortField = userQueryDTO.getSortField();
        String sortOrder = userQueryDTO.getSortOrder();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id)
                .eq(StrUtil.isNotBlank(userRole), "user_role", userRole)
                .like(StrUtil.isNotBlank(userAccount), "user_account", userAccount)
                .like(StrUtil.isNotBlank(userName), "user_name", userName)
                .like(StrUtil.isNotBlank(userProfile), "user_profile", userProfile)
                .orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }
}




