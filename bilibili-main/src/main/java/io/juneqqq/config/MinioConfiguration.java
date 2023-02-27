package io.juneqqq.config;


import io.minio.MinioAsyncClient;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class MinioConfiguration {

    @Resource
    private MinioProperties minioProperties;


    @Bean
    public PearlMinioClient minioClient() {
        MinioAsyncClient minioClient = MinioAsyncClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        return new PearlMinioClient(minioClient);
    }


    @Data
    @Component
    @ConfigurationProperties(prefix = "minio")
    public static class MinioProperties {
        /**
         * 连接地址
         */
        private String endpoint;
        /**
         * 用户名
         */
        private String accessKey;
        /**
         * 密码
         */
        private String secretKey;

        private String defaultBucket;

    }

}
