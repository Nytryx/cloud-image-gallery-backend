package com.nytryx.gallery.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图像标签分类前端返回类
 */
@Data
public class ImageTagCategory implements Serializable {

    private static final long serialVersionUID = 424427346725261721L;

    private List<String> tagList;

    private List<String> categoryList;
}
