package com.nytryx.gallery.manager;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.PutObjectRequest;
import com.nytryx.gallery.config.OSSClientConfig;
import com.nytryx.gallery.execption.BusinessExecption;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.execption.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;

@Slf4j
@Component
public class OSSManager {

    @Resource
    private OSSClientConfig ossClientConfig;

    @Resource
    private OSS ossClient;

    /**
     * 上传对象
     *
     * @param key         对象存储路径 + 名称
     * @param inputStream 文件对象
     */
    public void upload(String key, InputStream inputStream) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossClientConfig.getBucket(), key, inputStream);
            ossClient.putObject(putObjectRequest);
        } catch (OSSException oe) {
            log.error("上传时OSS服务端异常, errorCode: {}, message: {}", oe.getErrorCode(), oe.getErrorMessage());
            throw new BusinessExecption(ErrorCode.OPERATION_ERROR, "OSS服务异常，对象上传失败");
        } catch (ClientException ce) {
            log.error("上传时OSS客户端异常, message: {}", ce.getMessage());
            throw new BusinessExecption(ErrorCode.OPERATION_ERROR, "OSS服务异常，对象上传失败");
        }
    }

    /**
     * 下载对象
     *
     * @param key 对象存储路径 + 名称
     */
    public void download(String key) {
        try {
            GetObjectRequest getObjectRequest = new GetObjectRequest(ossClientConfig.getBucket(), key);
            ossClient.getObject(getObjectRequest);
        } catch (OSSException oe) {
            log.error("下载时OSS服务端异常, errorCode: {}, message: {}", oe.getErrorCode(), oe.getErrorMessage());
            throw new BusinessExecption(ErrorCode.OPERATION_ERROR, "OSS服务异常，对象下载失败");
        } catch (ClientException ce) {
            log.error("下载时OSS客户端异常, message: {}", ce.getMessage());
            throw new BusinessExecption(ErrorCode.OPERATION_ERROR, "OSS服务异常，对象下载失败");
        }
    }

    /**
     * 删除对象
     * @param key 对象路径 + 名称
     */
    public void delete(String key) {
        String deleteKey = StrUtil.removePrefix(key, "https://" + ossClientConfig.getBucket() + "." + ossClientConfig.getEndpoint() + "/");
        ossClient.deleteObject(ossClientConfig.getBucket(), deleteKey);
    }
}
