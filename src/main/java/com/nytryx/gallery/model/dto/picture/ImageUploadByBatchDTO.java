package com.nytryx.gallery.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片批量上传DTO
 */
@Data
public class ImageUploadByBatchDTO implements Serializable {
    private static final long serialVersionUID = -1270027652927569131L;
    /**
     * 搜索次
     */
    private String searchText;

    /**
     * 抓取数量（默认为10）
     */
    private Integer count = 10;

    /**
     * 图片名称前缀
     */
    private String namePrefix;

}
