package com.nytryx.gallery.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nytryx.gallery.model.dto.picture.ImageQueryDTO;
import com.nytryx.gallery.model.dto.picture.ImageReviewDTO;
import com.nytryx.gallery.model.dto.picture.ImageUploadByBatchDTO;
import com.nytryx.gallery.model.dto.picture.ImageUploadDTO;
import com.nytryx.gallery.model.entity.Image;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nytryx.gallery.model.entity.User;
import com.nytryx.gallery.model.vo.ImageVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * 图片服务接口
 * @author zhent
 */
public interface ImageService extends IService<Image> {

    /**
     * 图片校验
     * @param image 图片
     */
    void validImage(Image image);

    /**
     * 上传图片
     * @param fileSource 文件源
     * @param imageUploadDTO 图片上传请求（携带图片id信息）
     * @param loginUser 上传用户信息
     * @return  图片上传前端返回类
     */
    ImageVO imageUpload(Object fileSource, ImageUploadDTO imageUploadDTO, User loginUser);

    /**
     * 获取图片前端返回类
     * @param image 图片PO
     * @return 图片VO
     */
    ImageVO getImageVO(Image image);

    /**
     * 获取图片前端分页返回类
     * @param imagePage 图片PO分页对象
     * @return 图片VO分页对象
     */
    Page<ImageVO> getImageVOPage(Page<Image> imagePage);

    /**
     * 图片查询包装器
     * @param imageQueryDTO 图片查询DTO
     * @return 包装器
     */
    QueryWrapper<Image> getQueryWrapper(ImageQueryDTO imageQueryDTO);

    /**
     * 图片审核
     * @param imageReviewDTO 图片审核DTO
     * @param loginUser 审核用户
     */
    void doImageReview(ImageReviewDTO imageReviewDTO, User loginUser);

    /**
     * 填充审核参数
     * @param image 图片对象
     * @param loginuser 登录用户
     */
    void setImageReviewPass(Image image, User loginuser);

    /**
     * 批量抓取和创建图片
     *
     * @param imageUploadByBatchDTO 批量抓取请求类
     * @param loginUser             登录用户
     * @return 抓取数量
     */
    Integer imageUploadByBatch(ImageUploadByBatchDTO imageUploadByBatchDTO, User loginUser);

    /**
     * 删除图片文件
     * @param oldImage 图片对象
     */
    void deleteImageFile(Image oldImage);
}
