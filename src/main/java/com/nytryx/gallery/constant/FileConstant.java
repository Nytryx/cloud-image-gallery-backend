package com.nytryx.gallery.constant;

import java.util.Arrays;
import java.util.List;

public class FileConstant {

    public static final int ONE_M = 1024 * 1024;

    /**
     * 允许上传的后缀列表
     */
    public static final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "jpg", "webp");

    /**
     * OSS获取图片信息操作URL签名
     */
    public static final String OSS_GET_INFO_SIGN = "?x-oss-process=image/info";

    /**
     * OSS返回的图片高度信息字段对象名
     */
    public static final String OSS_IMAGE_INFO_HEIGHT = "ImageHeight";

    /**
     * OSS返回的图片宽度信息字段对象名
     */
    public static final String OSS_IMAGE_INFO_WIDTH = "ImageWidth";

    /**
     * OSS返回的图片大小信息字段对象名
     */
    public static final String OSS_IMAGE_INFO_SIZE = "FileSize";

    /**
     * OSS返回的图片类型信息字段对象名
     */
    public static final String OSS_IMAGE_INFO_FORMAT = "Format";

    /**
     * OSS返回的图片信息字段对象键名
     */
    public static final String OSS_IMAGE_INFO_OBJ_KEY = "value";

    /**
     * OSS公开图片存储路径前缀
     */
    public static final String OSS_PUBLIC_STORGE_PRE = "public";
}
