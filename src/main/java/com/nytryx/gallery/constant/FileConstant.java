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
     * OSS设置获取图片压缩签名
     */
    public static final String OSS_GET_FORMAT_SIGN = "?x-oss-process=image/resize,w_1200/quality,q_80/format,webp";

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

    /**
     * 允许通过URL获取的文件类型
     */
    public static final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");

    /**
     * 抓取图片并解析时，图片元素的类名
     */
    public static final String FETCH_IMAGES_ELEMENT_FIRST_CLASS_NAME = "dgControl";

    /**
     * 抓取图片并解析时，图片元素的第二层类名
     */
    public static final String FETCH_IMAGES_ELEMENT_SECOND_CLASS_NAME = "img.mimg";
}
