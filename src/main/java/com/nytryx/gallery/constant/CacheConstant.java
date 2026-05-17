package com.nytryx.gallery.constant;

import cn.hutool.core.util.RandomUtil;

public class CacheConstant {

    /**
     * redis中key的项目命名（取首字母缩写）
     */
    public static final String REDIS_PROJ_NAME = "cig";

    /**
     * redis过期时间，设置5 - 10分钟，避免缓存雪崩
     */
    public static final int REDIS_EXPIRE_TIME = 300 + RandomUtil.randomInt(0, 300);
}
