package com.nytryx.gallery.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传的结果
 */
@Data
public class ImageUploadResult implements Serializable {

    private static final long serialVersionUID = -3684640061503933811L;
    /**
     * 图片地址
     */
    private String url;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

}
