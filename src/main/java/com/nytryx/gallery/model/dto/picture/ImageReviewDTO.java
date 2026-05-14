package com.nytryx.gallery.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片审核DTO
 */
@Data
public class ImageReviewDTO implements Serializable {

    private static final long serialVersionUID = 8070903478891267714L;
    /**
     * 图片id
     */
    private Long id;

    /**
     * 审核状态：0-待审核；1-通过；2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;
}
