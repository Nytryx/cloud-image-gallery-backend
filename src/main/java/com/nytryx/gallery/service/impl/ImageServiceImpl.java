package com.nytryx.gallery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nytryx.gallery.execption.BusinessExecption;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.execption.ThrowUtils;
import com.nytryx.gallery.manager.FileManager;
import com.nytryx.gallery.manager.upload.FileImageUpload;
import com.nytryx.gallery.manager.upload.ImageUploadTemplate;
import com.nytryx.gallery.manager.upload.URLImageUpload;
import com.nytryx.gallery.model.dto.file.ImageUploadResult;
import com.nytryx.gallery.model.dto.picture.ImageQueryDTO;
import com.nytryx.gallery.model.dto.picture.ImageReviewDTO;
import com.nytryx.gallery.model.dto.picture.ImageUploadDTO;
import com.nytryx.gallery.model.entity.Image;
import com.nytryx.gallery.model.entity.User;
import com.nytryx.gallery.model.enums.ImageReviewStatusEnum;
import com.nytryx.gallery.model.vo.ImageVO;
import com.nytryx.gallery.model.vo.UserVO;
import com.nytryx.gallery.service.ImageService;
import com.nytryx.gallery.mapper.ImageMapper;
import com.nytryx.gallery.service.UserService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nytryx.gallery.constant.FileConstant.OSS_PUBLIC_STORGE_PRE;

/**
 * 图片服务接口实现类
 * @author zhent 
 */
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image>
    implements ImageService {

    @Resource
    private UserService userService;

    @Resource
    private FileImageUpload fileImageUpload;

    @Resource
    private URLImageUpload urlImageUpload;

    @Override
    public void validImage(Image image) {
        ThrowUtils.throwIf(image == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值  
        Long id = image.getId();
        String url = image.getUrl();
        String introduction = image.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验  
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public ImageVO imageUpload(Object fileSource, ImageUploadDTO imageUploadDTO, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 2.判断是新增还是更新
        Long imgId = null;
        if (imageUploadDTO != null) {
            imgId = imageUploadDTO.getId();
        }
        // 如果是更新图片则判断图片是否存在
        if (imgId != null) {
            Image oldImage = getById(imgId);
            ThrowUtils.throwIf(oldImage == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 判断权限，因为该接口管理员和普通用户都可以调用
            // 修改图片必须为管理员或者图片创建者
            ThrowUtils.throwIf(!userService.isAdmin(loginUser)
                    && !oldImage.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        }
        // 3.上传图片
        // 根据用户划分目录
        String uploadPathPrefix = String.format(OSS_PUBLIC_STORGE_PRE + "/" +loginUser.getId());
        // 根据fileSource类型使用不同的上传方式
        ImageUploadTemplate imageUploadTemplate = fileImageUpload;
        if (fileSource instanceof String) {
            imageUploadTemplate = urlImageUpload;
        }
        ImageUploadResult imageUploadResult = imageUploadTemplate.imageUpload(fileSource, uploadPathPrefix);
        // 4.构造入库图片信息并操作数据库
        Image image = setImageOSSInfo(loginUser, imageUploadResult, imgId);
        boolean result = save(image);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return ImageVO.po2Vo(image);
    }

    @Override
    public ImageVO getImageVO(Image image) {
        // 对象转封装类
        ImageVO imageVO = ImageVO.po2Vo(image);
        // 关联查询用户信息
        Long userId = image.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            imageVO.setUserVO(userVO);
        }
        return imageVO;
    }

    @Override
    public Page<ImageVO> getImageVOPage(Page<Image> imagePage) {
        List<Image> imageList = imagePage.getRecords();
        Page<ImageVO> imageVOPage = new Page<>(imagePage.getCurrent(), imagePage.getSize(), imagePage.getTotal());
        if (CollUtil.isEmpty(imageList)) {
            return imageVOPage;
        }
        // 对象列表 => 封装对象列表  
        List<ImageVO> imageVOList = imageList.stream().map(ImageVO::po2Vo).collect(Collectors.toList());
        // 1. 关联查询用户信息  
        Set<Long> userIdSet = imageList.stream().map(Image::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息  
        imageVOList.forEach(imageVO -> {
            Long userId = imageVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            imageVO.setUserVO(userService.getUserVO(user));
        });
        imageVOPage.setRecords(imageVOList);
        return imageVOPage;
    }


    @Override
    public QueryWrapper<Image> getQueryWrapper(ImageQueryDTO imageQueryDTO) {
        QueryWrapper<Image> imageQueryWrapper = new QueryWrapper<>();
        if (imageQueryDTO == null) {
            return imageQueryWrapper;
        }
        Long id = imageQueryDTO.getId();
        String name = imageQueryDTO.getName();
        String introduction = imageQueryDTO.getIntroduction();
        String category = imageQueryDTO.getCategory();
        List<String> tags = imageQueryDTO.getTags();
        Long picSize = imageQueryDTO.getPicSize();
        Integer picWidth = imageQueryDTO.getPicWidth();
        Integer picHeight = imageQueryDTO.getPicHeight();
        Double picScale = imageQueryDTO.getPicScale();
        String picFormat = imageQueryDTO.getPicFormat();
        String searchText = imageQueryDTO.getSearchText();
        Long userId = imageQueryDTO.getUserId();
        String sortField = imageQueryDTO.getSortField();
        String sortOrder = imageQueryDTO.getSortOrder();
        // 图片审核字段
        Long reviewId = imageQueryDTO.getReviewId();
        String reviewMessage = imageQueryDTO.getReviewMessage();
        Integer reviewStatus = imageQueryDTO.getReviewStatus();
        Date reviewTime = imageQueryDTO.getReviewTime();

        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            imageQueryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
            imageQueryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
            imageQueryWrapper.eq(ObjUtil.isNotEmpty(userId), "user_id", userId);
            imageQueryWrapper.like(StrUtil.isNotBlank(name), "name", name);
            imageQueryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
            imageQueryWrapper.like(StrUtil.isNotBlank(picFormat), "pic_format", picFormat);
            imageQueryWrapper.eq(ObjUtil.isNotEmpty(category), "category", category);
            imageQueryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "pic_width", picWidth);
            imageQueryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "pic_height", picHeight);
            imageQueryWrapper.eq(ObjUtil.isNotEmpty(picSize), "pic_size", picSize);
            imageQueryWrapper.eq(ObjUtil.isNotEmpty(picScale), "pic_scale", picScale);
            imageQueryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "review_status", reviewStatus);
            imageQueryWrapper.eq(ObjUtil.isNotEmpty(reviewId), "review_id", reviewId);
            imageQueryWrapper.like(StrUtil.isNotBlank(reviewMessage), "review_message", reviewMessage);
            // json数组查询
            if (CollUtil.isNotEmpty(tags)) {
                for (String tag : tags) {
                    imageQueryWrapper.like("tags", "\"" + tag + "\"");
                }
            }
            // 排序
            imageQueryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
            return imageQueryWrapper;
        }
        return null;
    }

    @Override
    public void doImageReview(ImageReviewDTO imageReviewDTO, User loginUser) {
        // 1.校验参数
        ThrowUtils.throwIf(imageReviewDTO == null, ErrorCode.PARAMS_ERROR);
        Long imageId = imageReviewDTO.getId();
        Integer reviewStatus = imageReviewDTO.getReviewStatus();
        String reviewMessage = imageReviewDTO.getReviewMessage();
        ImageReviewStatusEnum imageReviewStatusEnum = ImageReviewStatusEnum.getEnumByValue(reviewStatus);
        ThrowUtils.throwIf(imageId == null || imageReviewStatusEnum == null || reviewMessage == null, ErrorCode.PARAMS_ERROR);
        // 2.判断图片是否存在
        Image imageToReview = getById(imageId);
        ThrowUtils.throwIf(imageToReview == null, ErrorCode.NOT_FOUND_ERROR);
        // 3.校验审核状态是否重复
        if (imageToReview.getReviewStatus().equals(reviewStatus)) {
            // 如果是重复的，说明重复审核，抛出异常
            throw new BusinessExecption(ErrorCode.PARAMS_ERROR, "重复审核");
        }
        // 4.更新数据
        Image updateImage = new Image();
        BeanUtil.copyProperties(imageReviewDTO, updateImage);
        updateImage.setReviewerId(loginUser.getId());
        updateImage.setUpdateTime(new Date());
        boolean result = updateById(updateImage);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @NonNull
    private Image setImageOSSInfo(User loginUser, ImageUploadResult imageUploadResult, Long imgId) {
        Image image = new Image();
        image.setUrl(imageUploadResult.getUrl());
        image.setName(imageUploadResult.getPicName());
        image.setPicSize(imageUploadResult.getPicSize());
        image.setPicWidth(imageUploadResult.getPicWidth());
        image.setPicHeight(imageUploadResult.getPicHeight());
        image.setPicScale(imageUploadResult.getPicScale());
        image.setPicFormat(imageUploadResult.getPicFormat());
        image.setUserId(loginUser.getId());
        // 更新图片审核状态
        setImageReviewPass(image, loginUser);
        if (imgId != null) {
            // imgId不为空，更新数据，需要补充图片id和编辑时间信息
            image.setId(imgId);
            image.setEditTime(new Date());
        }
        return image;
    }

    /**
     * 填充审核参数
     * @param image 图片对象
     * @param loginuser 登录用户
     */
    public void setImageReviewPass(Image image, User loginuser) {
        if (userService.isAdmin(loginuser)) {
            // 管理员则自动过申
            image.setReviewStatus(ImageReviewStatusEnum.PASS.getValue());
            image.setReviewerId(loginuser.getId());
            image.setReviewMessage("管理员自动过审");
            image.setReviewTime(new Date());
        }
        // 非管理员用户， 无论是编辑图片还是新增图片，都设置为待审核状态
        image.setReviewStatus(ImageReviewStatusEnum.REVIEWING.getValue());
    }
}




