package com.nytryx.gallery.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nytryx.gallery.annotation.AuthCheck;
import com.nytryx.gallery.common.DeleteRequest;
import com.nytryx.gallery.common.Result;
import com.nytryx.gallery.constant.UserConstant;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.execption.ThrowUtils;
import com.nytryx.gallery.model.dto.picture.*;
import com.nytryx.gallery.model.entity.Image;
import com.nytryx.gallery.model.entity.User;
import com.nytryx.gallery.model.enums.ImageReviewStatusEnum;
import com.nytryx.gallery.model.enums.UserRoleEnum;
import com.nytryx.gallery.model.vo.ImageTagCategory;
import com.nytryx.gallery.model.vo.ImageVO;
import com.nytryx.gallery.service.ImageService;
import com.nytryx.gallery.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.nytryx.gallery.constant.CacheConstant.REDIS_EXPIRE_TIME;
import static com.nytryx.gallery.constant.CacheConstant.REDIS_PROJ_NAME;

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

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    /**
     * 图片上传
     * @param multipartFile  文件
     * @param imageUploadDTO 图片上传请求（携带图片id参数）
     * @param request        Http请求对象
     * @return 图片前端返回类对象
     */
    @PostMapping("/upload")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<ImageVO> imageUpload(
            @RequestPart("file") MultipartFile multipartFile,
            ImageUploadDTO imageUploadDTO,
            HttpServletRequest request
    ) {
        return Result.success(imageService.imageUpload(multipartFile, imageUploadDTO, userService.getLoginUser(request)));
    }

    /**
     * 图片上传（通过Url）
     * @param imageUploadDTO 图片上传请求（携带图片id参数或者Url参数）
     * @param request        Http请求对象
     * @return 图片前端返回类对象
     */
    @PostMapping("/upload/url")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<ImageVO> imageUploadByUrl(@RequestBody ImageUploadDTO imageUploadDTO, HttpServletRequest request) {
        String fileUrl = imageUploadDTO.getFileUrl();
        return Result.success(imageService.imageUpload(fileUrl, imageUploadDTO, userService.getLoginUser(request)));
    }

    /**
     * 图片删除
     * 图片创建者和管理员都可以删除，因此不做权限校验
     * @param deleteRequest 删除请求对象
     * @return 是否成功
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteImage(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest.getId() == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 验证图片是否为当前登录用户所创建
        User loginUser = userService.getLoginUser(request);
        Image image = imageService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(image == null, ErrorCode.NOT_FOUND_ERROR);
        // 如果当前登录用户不是管理员，且不是图片差创建者，则无法删除该图片
        ThrowUtils.throwIf(!loginUser.getId().equals(image.getUserId()) &&
                !UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR);
        boolean result = imageService.removeById(deleteRequest);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片删除失败");
        return Result.success(true);
    }

    /**
     * 图片更新（管理员）
     * 需要注意 Image 和 ImageUpdateDTO的tags字段存在类型差异，BeanUtil无法处理，需要手动操作
     * @param imageUpdateDTO 图片更新DTO
     * @param request Http请求对象
     * @return 图片更新是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> updateImage(@RequestBody ImageUpdateDTO imageUpdateDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(imageUpdateDTO.getId() == null ||imageUpdateDTO.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Image image = new Image();
        BeanUtil.copyProperties(imageUpdateDTO, image);
        image.setTags(JSONUtil.toJsonStr(imageUpdateDTO.getTags()));
        // 进行图片数据校验
        imageService.validImage(image);
        // 判断需要更新的数据是否存在
        long imageId = imageUpdateDTO.getId();
        Image imageToUpdate = imageService.getById(imageId);
        ThrowUtils.throwIf(imageToUpdate == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新图片审核状态
        imageService.setImageReviewPass(image, userService.getLoginUser(request));
        // 操作数据库
        boolean result = imageService.updateById(image);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 根据id获取图片（管理员）
     * @param id 图片id
     * @return 图片PO
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Image> getById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Image image = imageService.getById(id);
        ThrowUtils.throwIf(image == null, ErrorCode.NOT_FOUND_ERROR);
        return Result.success(image);
    }

    /**
     * 根据id获取图片
     * @param id 图片id
     * @return 图片VO
     */
    @GetMapping("/get/vo")
    public Result<ImageVO> getVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Image image = imageService.getById(id);
        ThrowUtils.throwIf(image == null, ErrorCode.NOT_FOUND_ERROR);
        return Result.success(imageService.getImageVO(image));
    }

    /**
     * 分页获取图片（管理员）
     * @param imageQueryDTO 图片查询DTO
     * @return 图片PO分页
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Page<Image>> listImageByPage(@RequestBody ImageQueryDTO imageQueryDTO) {
        int current = imageQueryDTO.getCurrent();
        int pageSize = imageQueryDTO.getPageSize();
        // 查询数据库
        return Result.success(imageService.page(new Page<>(current, pageSize), imageService.getQueryWrapper(imageQueryDTO)));
    }

    /**
     * 分页获取图片
     * @param imageQueryDTO 图片查询DTO
     * @return 图片VO分页
     */
    @PostMapping("/list/page/vo")
    public Result<Page<ImageVO>> listImageVOByPage(@RequestBody ImageQueryDTO imageQueryDTO) {
        int current = imageQueryDTO.getCurrent();
        int pageSize = imageQueryDTO.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户只能查询审核状态为通过的图片
        imageQueryDTO.setReviewStatus(ImageReviewStatusEnum.PASS.getValue());
        // 查询数据库
        Page<Image> page = imageService.page(new Page<>(current, pageSize), imageService.getQueryWrapper(imageQueryDTO));
        return Result.success(imageService.getImageVOPage(page));
    }

    /**
     * 分页获取图片（使用缓存）
     * @param imageQueryDTO 图片查询DTO
     * @return 图片VO分页
     */
    @PostMapping("/list/page/vo/cache")
    public Result<Page<ImageVO>> listImageVOByPageWithCache(@RequestBody ImageQueryDTO imageQueryDTO) {
        int current = imageQueryDTO.getCurrent();
        int pageSize = imageQueryDTO.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户只能查询审核状态为通过的图片
        imageQueryDTO.setReviewStatus(ImageReviewStatusEnum.PASS.getValue());
        // 查询数据库之前先查询数据库
        // 构建缓存key
        String queryCondition = JSONUtil.toJsonStr(imageQueryDTO);
        String queryConditionKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = String.format(REDIS_PROJ_NAME + ":listImageVOByPage:%s", queryConditionKey);
        // 查询缓存
        // 1.先查本地缓存
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            // 本地缓存存在，直接返回
            Page<ImageVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return Result.success(cachedPage);
        }
        // 2.本地缓存未命中，查询分布式缓存redis
        cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            // 分布式缓存成功命中，更新本地缓存后返回结果
            LOCAL_CACHE.put(cacheKey, cachedValue);
            Page<ImageVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return Result.success(cachedPage);
        }
        // 3.分布式缓存也为命中，查询数据库
        Page<Image> page = imageService.page(new Page<>(current, pageSize), imageService.getQueryWrapper(imageQueryDTO));
        Page<ImageVO> imageVOPage = imageService.getImageVOPage(page);
        String valueToCache = JSONUtil.toJsonStr(imageVOPage);
        // 更新本地缓存
        LOCAL_CACHE.put(cacheKey, valueToCache);
        // 更新分布式缓存，并设置缓存的过期时间，5 - 10分钟（避免缓存雪崩）
        stringRedisTemplate.opsForValue().set(cacheKey, valueToCache, REDIS_EXPIRE_TIME, TimeUnit.SECONDS);
        return Result.success(imageVOPage);
    }

    /**
     * 编辑图片
     * @param imageEditDTO 编辑图片DTO
     * @param request Http请求对象
     * @return 是否成功
     */
    @PostMapping("/edit")
    public Result<Boolean> editImage(@RequestBody ImageEditDTO imageEditDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(imageEditDTO.getId() == null || imageEditDTO.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Image image = new Image();
        BeanUtil.copyProperties(imageEditDTO, image);
        // tags字段类型不同，需要手动更新
        image.setTags(JSONUtil.toJsonStr(imageEditDTO.getTags()));
        // 判断更新目标是否存在
        Image imageToEdit = imageService.getById(imageEditDTO.getId());
        ThrowUtils.throwIf(imageToEdit == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 更新图片审核状态
        imageService.setImageReviewPass(image, loginUser);
        // 仅本人和管理员可编辑
        boolean canEdit = loginUser.getId().equals(imageToEdit.getUserId())
                || UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!canEdit, ErrorCode.NO_AUTH_ERROR);
        // 操作数据库
        boolean result = imageService.updateById(image);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 获取图片标签分类
     * @return 图片标签视图
     */
    @GetMapping("/tag_category")
    public Result<ImageTagCategory> listImageTagCategory() {
        ImageTagCategory imageTagCategory = new ImageTagCategory();
        List<String> tagList = Arrays.asList("热面", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模版", "电商", "表情包", "素材", "海报");
        imageTagCategory.setTagList(tagList);
        imageTagCategory.setCategoryList(categoryList);
        return Result.success(imageTagCategory);
    }

    /**
     * 审核图片
     * @param imageReviewDTO 图片审核DTO
     * @param request Http请求对象
     * @return 是否成功
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> doImageReview(@RequestBody ImageReviewDTO imageReviewDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(imageReviewDTO.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取当前登录的用户
        User loginUser = userService.getLoginUser(request);
        imageService.doImageReview(imageReviewDTO, loginUser);
        return Result.success(true);
    }

    /**
     * 批量上传图片
     * @param imageUploadByBatchDTO 图片批量上传请求对象
     * @param request Http请求对象
     * @return 是否成功
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Integer> imageUploadByBatch(@RequestBody ImageUploadByBatchDTO imageUploadByBatchDTO, HttpServletRequest request) {
        // 获取当前登录的用户
        User loginUser = userService.getLoginUser(request);
        Integer uploadCount = imageService.imageUploadByBatch(imageUploadByBatchDTO, loginUser);
        return Result.success(uploadCount);
    }
}
