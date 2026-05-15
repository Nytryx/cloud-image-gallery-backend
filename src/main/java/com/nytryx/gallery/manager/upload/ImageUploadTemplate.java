package com.nytryx.gallery.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.nytryx.gallery.config.OSSClientConfig;
import com.nytryx.gallery.execption.BusinessExecption;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.manager.OSSManager;
import com.nytryx.gallery.model.dto.file.ImageUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static com.nytryx.gallery.constant.FileConstant.OSS_GET_INFO_SIGN;
import static com.nytryx.gallery.constant.FileConstant.OSS_IMAGE_INFO_FORMAT;
import static com.nytryx.gallery.constant.FileConstant.OSS_IMAGE_INFO_HEIGHT;
import static com.nytryx.gallery.constant.FileConstant.OSS_IMAGE_INFO_OBJ_KEY;
import static com.nytryx.gallery.constant.FileConstant.OSS_IMAGE_INFO_SIZE;
import static com.nytryx.gallery.constant.FileConstant.OSS_IMAGE_INFO_WIDTH;

@Slf4j
@Component
public abstract class ImageUploadTemplate {
    @Resource
    private OSSClientConfig ossClientConfig;

    @Resource
    private OSSManager ossManager;

    /**
     * 上传图片
     *
     * @param source    文件
     * @param uploadPathPrefix 文件在OSS中的存储路径前缀（例如，用户ID）
     * @return 自定义图片上传返回类（包含图片信息）
     */
    public ImageUploadResult imageUpload(Object source, String uploadPathPrefix) {
        // 校验图片
        validateImg(source);
        // 自定义图片上传地址构造
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(source);
        String uploadFileName = String.format("%s_%s.%s",
                DateUtil.formatDate(new Date()),
                uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadFilePath = String.format("%s/%s", uploadPathPrefix, uploadFileName);
        try {
            // 获取文件的输入流
            InputStream inputStream = getResourceInputStream(source);
            return buildResult(uploadFilePath, inputStream, originalFilename);
        } catch (Exception e) {
            log.error("上传图片失败: {}", e.getMessage());
            throw new BusinessExecption(ErrorCode.SYSTEM_ERROR, "文件上传失败，请重试");
        }
    }

    /**
     * 校验图片
     * @param source 文件源
     */
    protected abstract void validateImg(Object source);

    /**
     * 获取文件名
     * @param source 文件源
     * @return 输入流
     */
    protected abstract String getOriginalFilename(Object source);

    /**
     * 获取文件的输入流
     * @param source 文件源
     * @return 输入流
     */
    protected abstract InputStream getResourceInputStream(Object source) throws Exception;

    /**
     * 封装返回结果
     * @param uploadFilePath OSS中存储的路径
     * @param inputStream 文件输入流
     * @param originalFilename 原始文件名
     * @return 图片上传结果封装对象
     */
    private ImageUploadResult buildResult(String uploadFilePath, InputStream inputStream, String originalFilename) {
        ossManager.upload(uploadFilePath, inputStream);
        String url = "https://" + ossClientConfig.getBucket() + "." + ossClientConfig.getEndpoint() + "/" + uploadFilePath;
        String imageImfoUrl = url + OSS_GET_INFO_SIGN;
        // 通过分析OSS返回的信息
        String jsonResponse = HttpUtil.get(imageImfoUrl);
        JSONObject imageInfo = new JSONObject(jsonResponse);
        int imageWidth = imageInfo.getJSONObject(OSS_IMAGE_INFO_WIDTH).getInt(OSS_IMAGE_INFO_OBJ_KEY);
        int imageHeight = imageInfo.getJSONObject(OSS_IMAGE_INFO_HEIGHT).getInt(OSS_IMAGE_INFO_OBJ_KEY);
        long imageSize = imageInfo.getJSONObject(OSS_IMAGE_INFO_SIZE).getLong(OSS_IMAGE_INFO_OBJ_KEY);
        String imageFormat = imageInfo.getJSONObject(OSS_IMAGE_INFO_FORMAT).getStr(OSS_IMAGE_INFO_OBJ_KEY);
        double imageScale = NumberUtil.round(imageWidth * 1.0 / imageHeight, 2).doubleValue();

        ImageUploadResult imageUploadResult = new ImageUploadResult();
        imageUploadResult.setUrl(url);
        imageUploadResult.setPicName(FileUtil.mainName(originalFilename));
        imageUploadResult.setPicSize(imageSize);
        imageUploadResult.setPicWidth(imageWidth);
        imageUploadResult.setPicHeight(imageHeight);
        imageUploadResult.setPicScale(imageScale);
        imageUploadResult.setPicFormat(imageFormat);

        return imageUploadResult;
    }

}
