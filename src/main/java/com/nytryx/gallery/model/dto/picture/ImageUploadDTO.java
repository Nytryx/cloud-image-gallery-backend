package com.nytryx.gallery.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传DTO
 */
@Data
public class ImageUploadDTO implements Serializable {

    private static final long serialVersionUID = -2319837473540070261L;
    /**
     * 图片id
     */
    private Long id;

    /**
     * 文件地址
     */
    private String fileUrl;

}
