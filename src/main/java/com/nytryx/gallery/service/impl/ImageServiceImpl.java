package com.nytryx.gallery.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.execption.ThrowUtils;
import com.nytryx.gallery.manager.FileManager;
import com.nytryx.gallery.model.dto.file.ImageUploadResult;
import com.nytryx.gallery.model.dto.picture.ImageQueryDTO;
import com.nytryx.gallery.model.dto.picture.ImageUploadDTO;
import com.nytryx.gallery.model.entity.Image;
import com.nytryx.gallery.model.entity.User;
import com.nytryx.gallery.model.vo.ImageVO;
import com.nytryx.gallery.model.vo.UserVO;
import com.nytryx.gallery.service.ImageService;
import com.nytryx.gallery.mapper.ImageMapper;
import com.nytryx.gallery.service.UserService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
    private FileManager fileManager;

    @Resource
    private UserService userService;

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
    public ImageVO imageUpload(MultipartFile multipartFile, ImageUploadDTO imageUploadDTO, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 2.判断是新增还是更新
        Long imgId = null;
        if (imageUploadDTO != null) {
            imgId = imageUploadDTO.getId();
        }
        // 如果是更新图片则判断图片是否存在
        if (imgId != null) {
            boolean exists = lambdaQuery()
                    .eq(Image::getId, imgId)
                    .exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 3.上传图片
        // 根据用户划分目录
        String uploadPathPrefix = String.format(OSS_PUBLIC_STORGE_PRE + "/" +loginUser.getId());
        ImageUploadResult imageUploadResult = fileManager.imageUpload(multipartFile, uploadPathPrefix);
        // 4.构造入库图片信息并操作数据库
        Image image = getImage(loginUser, imageUploadResult, imgId);
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

    @NonNull
    private Image getImage(User loginUser, ImageUploadResult imageUploadResult, Long imgId) {
        Image image = new Image();
        image.setUrl(imageUploadResult.getUrl());
        image.setName(imageUploadResult.getPicName());
        image.setPicSize(imageUploadResult.getPicSize());
        image.setPicWidth(imageUploadResult.getPicWidth());
        image.setPicHeight(imageUploadResult.getPicHeight());
        image.setPicScale(imageUploadResult.getPicScale());
        image.setPicFormat(imageUploadResult.getPicFormat());
        image.setUserId(loginUser.getId());
        if (imgId != null) {
            // imgId不为空，更新数据，需要补充图片id和编辑时间信息
            image.setId(imgId);
            image.setEditTime(new Date());
        }
        return image;
    }
}




