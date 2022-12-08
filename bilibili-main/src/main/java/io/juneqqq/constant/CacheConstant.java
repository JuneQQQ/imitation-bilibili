package io.juneqqq.constant;

import java.time.Duration;

public class CacheConstant {

    /**
     * 本项目 Redis 缓存前缀
     */
    public static final String REDIS_CACHE_PREFIX = "Cache:Bilibili:";

    /**
     * Caffeine 缓存管理器 bean 名
     */
    public static final String CACHE_TYPE_CAFFEINE = "caffeineCacheManager";

    /**
     * Redis 缓存管理器 bean 名
     */
    public static final String CACHE_TYPE_REDIS = "redisCacheManager";


    /**
     * 图片验证码缓存 KEY
     */
    public static final String IMG_VERIFY_CODE_CACHE_KEY =
            REDIS_CACHE_PREFIX + "imgVerifyCodeCache:";

    /**
     * 用户信息缓存
     */
    public static final String USER_INFO_CACHE_NAME = "userInfoCache";
    public static final String FILE_FINAL_CACHE_NAME = "file:hashToUploadId:";
    public static final String FILE_SIZE_CACHE_NAME = "file:size:";
    public static final String USER_SUBSCRIBED_CACHE_NAME = "subscribed:";
    public static final String DANMUA_CACHE_NAME = "dm:video:";

    /**
     * 缓存过期时间和最大数量配置，仅作用于
     * io.juneqqq.config.CacheConfig
     */
    public enum CacheEnum {
        USER_INFO_CACHE(USER_INFO_CACHE_NAME, Duration.ofDays(7).toSeconds(), 1, CACHE_TYPE_REDIS),
        FILE_FINAL_CACHE(FILE_FINAL_CACHE_NAME, Duration.ofDays(1).toSeconds(), 1, CACHE_TYPE_REDIS),
        FILE_SIZE_CACHE(FILE_SIZE_CACHE_NAME, Duration.ofDays(1).toSeconds(), 1, CACHE_TYPE_REDIS),
        USER_SUBSCRIBED_CACHE(USER_SUBSCRIBED_CACHE_NAME, Duration.ofDays(1).toSeconds(), 1, CACHE_TYPE_REDIS),
        DANMUA_CACHE(DANMUA_CACHE_NAME, Duration.ofDays(1).toSeconds(), 1, CACHE_TYPE_REDIS);
        private final String name;  // 缓存的名字
        private final long ttl; // 失效时间（秒） 0-永不失效
        private final int maxSize; //  最大容量
        private final String cacheType;

        CacheEnum(String name, long ttl, int maxSize, String cacheType) {
            this.name = name;
            this.ttl = ttl;
            this.maxSize = maxSize;
            this.cacheType = cacheType;
        }

        public String getCacheType() {
            return cacheType;
        }

        public String getName() {
            return name;
        }

        public long getTtl() {
            return ttl;
        }

        public int getMaxSize() {
            return maxSize;
        }
    }
}
