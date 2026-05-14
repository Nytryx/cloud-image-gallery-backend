package com.nytryx.gallery.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 图片审核状态枚举
 */
@Getter
public enum ImageReviewStatusEnum {

    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;

    private final int value;

    ImageReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举对象
     */
    public static ImageReviewStatusEnum getEnumByValue(int value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ImageReviewStatusEnum imageReviewStatusEnum : ImageReviewStatusEnum.values()) {
            if (imageReviewStatusEnum.value == value) {
                return imageReviewStatusEnum;
            }
        }
        return null;
    }
}
