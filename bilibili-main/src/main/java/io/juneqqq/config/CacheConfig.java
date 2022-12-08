package io.juneqqq.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.juneqqq.constant.CacheConstant;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class CacheConfig {
    /**
     * Caffeine 缓存管理器
     */
    @Bean(CacheConstant.CACHE_TYPE_CAFFEINE)
    @Primary
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<CaffeineCache> caches = new ArrayList<>(CacheConstant.CacheEnum.values().length);
        for (CacheConstant.CacheEnum c : CacheConstant.CacheEnum.values()) {
            if (c.getCacheType() == CacheConstant.CACHE_TYPE_CAFFEINE) {
                Caffeine<Object, Object> caffeine = Caffeine.newBuilder().recordStats().maximumSize(c.getMaxSize());
                if (c.getTtl() > 0) {
                    caffeine.expireAfterWrite(Duration.ofSeconds(c.getTtl()));
                }
                // <=0 永久有效
                caches.add(new CaffeineCache(c.getName(), caffeine.build()));
            }
        }
        cacheManager.setCaches(caches);
        return cacheManager;
    }

    /**
     * redis 缓存管理器
     */
    @Bean(CacheConstant.CACHE_TYPE_REDIS)
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);

        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues().prefixCacheNameWith(CacheConstant.REDIS_CACHE_PREFIX)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new FastJsonRedisSerializer<>(Object.class)));

        Map<String, RedisCacheConfiguration> cacheMap = new LinkedHashMap<>(CacheConstant.CacheEnum.values().length);
        for (CacheConstant.CacheEnum c : CacheConstant.CacheEnum.values()) {
            if (c.getCacheType() == CacheConstant.CACHE_TYPE_REDIS) {
                if (c.getTtl() > 0) {
                    cacheMap.put(c.getName(), RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues()
                            .prefixCacheNameWith(CacheConstant.REDIS_CACHE_PREFIX).entryTtl(Duration.ofSeconds(c.getTtl())));
                } else {
                    // <=0 永久有效
                    cacheMap.put(c.getName(), RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues()
                            .prefixCacheNameWith(CacheConstant.REDIS_CACHE_PREFIX));
                }
            }
        }

        RedisCacheManager redisCacheManager = new RedisCacheManager(redisCacheWriter,
                defaultCacheConfig, cacheMap);
        redisCacheManager.setTransactionAware(true);
        redisCacheManager.initializeCaches();
        return redisCacheManager;
    }

}
