package com.nytryx.gallery.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.execption.ThrowUtils;
import com.nytryx.gallery.model.entity.Image;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片前端返回类
 */
@Data
public class ImageVO implements Serializable {

    private static final long serialVersionUID = 7479600269424164093L;
    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建图片用户的信息
     */
    private UserVO userVO;


    /**
     * VO转PO
     */
    public static Image vo2Po(ImageVO imageVO) {
        ThrowUtils.throwIf(imageVO == null, ErrorCode.SYSTEM_ERROR);
        Image image = new Image();
        BeanUtil.copyProperties(imageVO, image);
        // tags字段类型不同，需要手动转换
        image.setTags(JSONUtil.toJsonStr(imageVO.getTags()));
        return image;
    }

    /**
     * PO转VO
     */
    public static ImageVO po2Vo(Image image) {
        ThrowUtils.throwIf(image == null, ErrorCode.SYSTEM_ERROR);
        ImageVO imageVO = new ImageVO();
        BeanUtil.copyProperties(image, imageVO);
        imageVO.setTags(JSONUtil.toList(image.getTags(), String.class));
        return imageVO;
    }
}
