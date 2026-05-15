package com.nytryx.gallery.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.nytryx.gallery.execption.BusinessExecption;
import com.nytryx.gallery.execption.ErrorCode;
import com.nytryx.gallery.execption.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import static com.nytryx.gallery.constant.FileConstant.ALLOW_CONTENT_TYPES;
import static com.nytryx.gallery.constant.FileConstant.ONE_M;

@Service
public class URLImageUpload extends ImageUploadTemplate {
    @Override
    protected void validateImg(Object source) {
        String fileUrl = (String) source;
        // 校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址为空");
        // 校验url格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessExecption(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        // 校验url协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或者 HTTPS 协议的文件地址");
        // 发送HEAD请求验证文件是否存在，验证一些信息
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl)
                    .execute();
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                // 未正常返回，无需执行其他判断，而不是抛出异常。因为某些服务器是不支持HEAD请求的
                return;
            }
            // 文件存在，进行文件类型的校验
            String contentType = httpResponse.header("Content-Type");
            // 若不为空，校验是否合法
            if (StrUtil.isNotBlank(contentType)) {
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 文件存在，进行文件大小的校验
            String contentLengthStr = httpResponse.header("Content-Length");
            // 若不为空，校验是否合法
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    ThrowUtils.throwIf(contentLength > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
                } catch (NumberFormatException e) {
                    throw new BusinessExecption(ErrorCode.PARAMS_ERROR, "文件大小格式异常");
                }
            }
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

    @Override
    protected String getOriginalFilename(Object source) {
        String fileUrl = (String) source;
        return FileUtil.getName(fileUrl);
    }

    @Override
    protected InputStream getResourceInputStream(Object source) throws Exception {
        String fileUrl = (String) source;
        URL urlForStream = new URL(fileUrl);
        // 注意：这里使用openStream，不易配置超时时间
        return urlForStream.openStream();
    }
}
