package com.nytryx.gallery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync // 开启异步支持（通过AOP实现的）
@SpringBootApplication
// MyBatis-Plus的使用需要添加@MapperSacn注解
@MapperScan("com.nytryx.gallery.mapper")
// 允许通过 AopContext.currentProxy() 获取AOP实现的代理类
@EnableAspectJAutoProxy(exposeProxy = true)
public class CloudImageGalleryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudImageGalleryBackendApplication.class, args);
    }

}
