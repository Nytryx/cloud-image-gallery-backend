package com.nytryx.gallery.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.execption.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static com.nytryx.gallery.constant.FileConstant.ALLOW_FORMAT_LIST;
import static com.nytryx.gallery.constant.FileConstant.ONE_M;

@Service
public class FileImageUpload extends ImageUploadTemplate {
    @Override
    protected void validateImg(Object source) {
        MultipartFile multipartFile = (MultipartFile) source;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1.校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > 20 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 20MB");
        // 2.校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型不支持");
    }

    @Override
    protected String getOriginalFilename(Object source) {
        MultipartFile multipartFile = (MultipartFile) source;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected InputStream getResourceInputStream(Object source) throws IOException {
        MultipartFile multipartFile = (MultipartFile) source;
        return multipartFile.getInputStream();
    }
}
