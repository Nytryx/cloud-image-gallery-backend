package com.nytryx.gallery.model.dto.picture;

import java.io.Serializable;
import java.util.List;

/**
 * 图片更新DTO （管理员）
 */
public class ImageUpdateDTO implements Serializable {

    private static final long serialVersionUID = -6712673524211667212L;

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
