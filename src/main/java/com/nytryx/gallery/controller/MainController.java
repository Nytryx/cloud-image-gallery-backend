package com.nytryx.gallery.controller;

import com.nytryx.gallery.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查接口（往往是一个很简单的接口，用于判断项目是否正常启动）
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("ok");
    }
}
