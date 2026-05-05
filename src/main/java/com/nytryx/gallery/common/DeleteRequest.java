package com.nytryx.gallery.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求类
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = -5323426777695388144L;

    /**
     * 目标数据的ID
     */
    private Long id;
}
