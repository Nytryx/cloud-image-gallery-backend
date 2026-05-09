package com.nytryx.gallery.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.nytryx.gallery.config.OSSClientConfig;
import com.nytryx.gallery.execption.BusinessExecption;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.execption.ThrowUtils;
import com.nytryx.gallery.model.dto.file.ImageUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.io.InputStream;
import java.util.Date;

import static com.nytryx.gallery.constant.FileConstant.*;

@Slf4j
@Service
public class FileManager {

    @Resource
    private OSSClientConfig ossClientConfig;

    @Resource
    private OSSManager ossManager;


    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 文件在OSS中的存储路径前缀（例如，用户ID）
     * @return 自定义图片上传返回类（包含图片信息）
     */
    public ImageUploadResult imageUpload(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validateImg(multipartFile);
        // 自定义图片上传地址构造
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFileName = String.format("%s_%s.%s",
                DateUtil.formatDate(new Date()),
                uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadFilePath = String.format("%s/%s", uploadPathPrefix, uploadFileName);
        try {
            // 通过InputStream上传文件
            InputStream inputStream = multipartFile.getInputStream();
            ossManager.upload(uploadFilePath, inputStream);
            String url = "https://" + ossClientConfig.getBucket() + "." + ossClientConfig.getEndpoint() + "/" + uploadFilePath + OSS_GET_INFO_SIGN;
            // 通过分析OSS返回的信息
            String jsonResponse = HttpUtil.get(url);
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
        } catch (Exception e) {
            log.error("上传图片失败: {}", e.getMessage());
            throw new BusinessExecption(ErrorCode.SYSTEM_ERROR, "文件上传失败，请重试");
        }
    }

    /**
     * 图片检验
     *
     * @param multipartFile 文件
     */
    private void validateImg(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1.校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
        // 2.校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型不支持");
    }

}
