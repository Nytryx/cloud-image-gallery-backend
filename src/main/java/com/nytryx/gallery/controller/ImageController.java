package com.nytryx.gallery.controller;

import com.nytryx.gallery.annotation.AuthCheck;
import com.nytryx.gallery.common.DeleteRequest;
import com.nytryx.gallery.common.Result;
import com.nytryx.gallery.constant.UserConstant;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.execption.ThrowUtils;
import com.nytryx.gallery.model.dto.picture.ImageUpdateDTO;
import com.nytryx.gallery.model.dto.picture.ImageUploadDTO;
import com.nytryx.gallery.model.entity.Image;
import com.nytryx.gallery.model.entity.User;
import com.nytryx.gallery.model.enums.UserRoleEnum;
import com.nytryx.gallery.model.vo.ImageVO;
import com.nytryx.gallery.service.ImageService;
import com.nytryx.gallery.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图片接口
 *
 * @author zhent
 */
@RestController
@RequestMapping("/picture")
public class ImageController {

    @Resource
    private UserService userService;

    @Resource
    private ImageService imageService;

    /**
     * 图片上传
     * @param multipartFile  文件
     * @param imageUploadDTO 图片上传请求（携带图片id参数）
     * @param request        Http请求对象
     * @return 图片前端返回类对象
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<ImageVO> imageUpload(
            @RequestPart("file") MultipartFile multipartFile,
            ImageUploadDTO imageUploadDTO,
            HttpServletRequest request
    ) {
        return Result.success(imageService.imageUpload(multipartFile, imageUploadDTO, userService.getLoginUser(request)));
    }

    /**
     * 图片删除
     * 图片创建者和管理员都可以删除，因此不做权限校验
     * @param deleteRequest 删除请求对象
     * @return 是否成功
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteImage(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 验证图片是否为当前登录用户所创建
        User loginUser = userService.getLoginUser(request);
        Image image = imageService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(image == null, ErrorCode.NOT_FOUND_ERROR);
        // 如果当前登录用户不是管理员，且不是图片差创建者，则无法删除该图片
        ThrowUtils.throwIf(!loginUser.getId().equals(image.getUserId()) &&
                UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR);
        boolean result = imageService.removeById(deleteRequest);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片删除失败");
        return Result.success(true);
    }
}
