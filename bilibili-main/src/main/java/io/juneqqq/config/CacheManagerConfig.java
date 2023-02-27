//package io.juneqqq.config;
//
//import com.alibaba.fastjson2.JSONReader;
//import com.alibaba.fastjson2.JSONWriter;
//import com.alibaba.fastjson2.support.config.FastJsonConfig;
//import com.alibaba.fastjson2.support.spring.data.redis.FastJsonRedisSerializer;
//import com.github.benmanes.caffeine.cache.Caffeine;
//import io.juneqqq.cache.CacheConstant;
//import jakarta.annotation.Resource;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.caffeine.CaffeineCache;
//import org.springframework.cache.support.CompositeCacheManager;
//import org.springframework.cache.support.SimpleCacheManager;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.redis.cache.*;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.serializer.*;
//
//import java.time.Duration;
//import java.util.*;
//
//@Configuration
//public class CacheManagerConfig {
//    @Resource
//    private ApplicationContext context;
//
//    /**
//     * Caffeine 缓存管理器
//     */
//    @Primary
//    @Bean(CacheConstant.LOCAL)
//    public CacheManager caffeineCacheManager() {
//        SimpleCacheManager cacheManager = new SimpleCacheManager();
//        List<CaffeineCache> caches = new ArrayList<>(16);
//
//        for (CacheConstant.CacheTTLAndMaxSizeConfig c : CacheConstant.CacheTTLAndMaxSizeConfig.values()) {
//            Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
//                    .recordStats()
//                    .maximumSize(c.getMaxSize()
//                    );
//            if (c.getTtl() > 0) {
//                caffeine.expireAfterWrite(Duration.ofSeconds(c.getTtl()));
//            }
//            // <=0 永久有效
//            caches.add(new CaffeineCache(c.getName(), caffeine.build()));
//        }
//
//        cacheManager.setCaches(caches);
//        return cacheManager;
//    }
//
//    /**
//     * redis 缓存管理器
//     */
//    @Bean(CacheConstant.REMOTE)
//    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
//        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
//
//        Map<String, Object> annotatedBeans = context.getBeansWithAnnotation(SpringBootApplication.class);
//        String mainClassPath = annotatedBeans.isEmpty() ? null : annotatedBeans.values().toArray()[0].getClass().getName();
//        assert mainClassPath != null;
//
//        // 定制value序列化器
//        FastJsonRedisSerializer<Object> serializer = new FastJsonRedisSerializer<>(Object.class);
//        FastJsonConfig fastJsonConfig = serializer.getFastJsonConfig();
//        fastJsonConfig.setWriterFeatures(JSONWriter.Feature.WriteClassName);
//        fastJsonConfig.setReaderFeatures(JSONReader.Feature.SupportAutoType); // 完全支持反序列化有类型安全问题
//        // 指定可以反序列化的白名单  // ParserConfig.getGlobalInstance().addAccept(mainClassPath.substring(0, mainClassPath.lastIndexOf(".") + 1));
//        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration
//                .defaultCacheConfig()
//                .prefixCacheNameWith(CacheConstant.REDIS_CACHE_PREFIX)
////                .computePrefixWith(n -> n + ":")   // 单冒号而不是双冒号
//                .serializeValuesWith(RedisSerializationContext.SerializationPair
//                        .fromSerializer(serializer));
//
//
//        Map<String, RedisCacheConfiguration> cacheMap = new LinkedHashMap<>(CacheConstant.CacheTTLAndMaxSizeConfig.values().length);
//        // 个性化定制
//        for (CacheConstant.CacheTTLAndMaxSizeConfig c : CacheConstant.CacheTTLAndMaxSizeConfig.values()) {
//            cacheMap.put(c.getName(), defaultCacheConfig
//                            .prefixCacheNameWith(CacheConstant.REDIS_CACHE_PREFIX)
//                            .serializeValuesWith(RedisSerializationContext.SerializationPair
//                                    .fromSerializer(serializer))
//                        .entryTtl(Duration.ofSeconds(c.getTtl()))
//            );
//        }
//
//        RedisCacheManager redisCacheManager =
//                new RedisCacheManager(redisCacheWriter, defaultCacheConfig, cacheMap);
//
//        redisCacheManager.setTransactionAware(true);
//        redisCacheManager.initializeCaches();
//        return redisCacheManager;
//    }
//
//    @Bean(CacheConstant.CACHE_TYPE_REDIS_AND_CAFFEINE)
//    public CacheManager combined(RedisCacheManager redisCacheManager, CacheManager caffeineCacheManager) {
//        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();
//        compositeCacheManager.setCacheManagers(Arrays.asList(caffeineCacheManager,redisCacheManager));
//
//        return compositeCacheManager;
//    }
//
//
//}
