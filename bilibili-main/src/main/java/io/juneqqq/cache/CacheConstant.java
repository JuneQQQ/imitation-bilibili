package io.juneqqq.cache;

import lombok.Getter;

public class CacheConstant {

    /**
     * 本项目 Redis 缓存前缀
     */
    public static final String REDIS_CACHE_PREFIX = "bilibili:cache:";

    /**
     * 用户信息缓存 -> UserInfo
     */

    public static final String USER_INFO = "user:user-info-cache:";

    /**
     * 视频信息缓存 -> Video
     */
    public static final String VIDEO_INFO = "video:video-info-cache";
    /**
     * 弹幕信息缓存 -> Danmu
     */
    public static final String DANMU = "danmu:danmu-cache:";

    /**
     * 用户 refreshToken ，目前是手动控制，所以需要加前缀
     */
    public static final String USER_REFRESH_TOKEN = "user:refresh-token:";
    /**
     * 分片上传时，文件上传id
     */
    public static final String FILE_FINAL = "file:hash-to-upload-id:";
    /**
     * 文件大小缓存
     */
    public static final String FILE_SIZE = "file:size:";


    /**
     * 用户订阅缓存
     */
    public static final String USER_SUBSCRIBED = "user:subscribed:";

}
