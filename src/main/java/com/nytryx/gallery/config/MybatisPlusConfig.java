package com.nytryx.gallery.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Slf4j
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    /**
     * MyBatis Plus 拦截器配置 （3.5.9版本之后需要手动配置）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 如果配置多个插件, 切记分页最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        return interceptor;
    }


    @Override
    public void insertFill(MetaObject metaObject) {}

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充");
        this.strictUpdateFill(metaObject, "editTime", Date.class, new Date());
    }
}