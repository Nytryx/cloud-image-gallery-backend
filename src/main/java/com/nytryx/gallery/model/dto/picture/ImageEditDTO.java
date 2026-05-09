package com.nytryx.gallery.model.dto.picture;

import java.io.Serializable;
import java.util.List;

/**
 * 图片编辑DTO
 */
public class ImageEditDTO implements Serializable {
    private static final long serialVersionUID = 8428044722088868764L;

    /**
     * id
     */
    private Long id;

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
     * 标签
     */
    private List<String> tags;
}
