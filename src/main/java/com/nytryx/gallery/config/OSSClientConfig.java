package com.nytryx.gallery.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oss.client")
@Data
public class OSSClientConfig {

    /**
     * 域名
     */
    private String endpoint;

    /**
     * 访问密钥id
     */
    private String accessKeyId;

    /**
     * 访问密钥
     */
    private String accessKeySecret;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    @Bean
    public OSS ossClient() {
        DefaultCredentialProvider defaultCredentialProvider =
                CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId, accessKeySecret);
        return OSSClientBuilder.create()
                .endpoint(endpoint)
                .region(region)
                .credentialsProvider(defaultCredentialProvider)
                .build();
    }
}
