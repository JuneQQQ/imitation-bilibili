package io.juneqqq.config;

import cn.hutool.log.Log;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties pool) {
        return new ThreadPoolExecutor(pool.getCoreSize(), pool.getCoreSize(), pool.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    @ConfigurationProperties(prefix = "service-thread-pool")
    @Component
    @Data
    public static class ThreadPoolConfigProperties {
        private Integer coreSize;
        private Integer maxSize;
        private Integer keepAliveTime;
    }
}
