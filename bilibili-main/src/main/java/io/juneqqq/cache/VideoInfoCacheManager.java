package io.juneqqq.cache;

import cn.hutool.core.bean.BeanUtil;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import io.juneqqq.pojo.dao.entity.Video;
import io.juneqqq.pojo.dto.cache.CacheVideoInfoDto;
import io.juneqqq.pojo.dto.database.VideoLCC;
import io.juneqqq.service.common.VideoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * 用户信息 缓存管理类
 */
@Component
@Slf4j
public class VideoInfoCacheManager {

    @Resource
    VideoService videoService;
    @Resource
    UserInfoCacheManager userInfoCacheManager;

    /**
     * 查询用户信息，并放入缓存中
     * unless = "#result == null"  不缓存空值
     */
//    @Cacheable(cacheManager = CacheConstant.REMOTE,
//            value = CacheConstant.VIDEO_INFO)
    @Cached(name = CacheConstant.VIDEO_INFO, expire = 3600, cacheType = CacheType.REMOTE, key = "#userId")
    public CacheVideoInfoDto getVideoInfo(Long videoId) {
        VideoLCC videoLCC = videoService.getVideoLCC(videoId);
        Video dbVideo = videoService.getVideoById(videoId);

        CacheVideoInfoDto cacheVideoInfoDto = new CacheVideoInfoDto();


        BeanUtil.copyProperties(videoLCC, cacheVideoInfoDto);
        BeanUtil.copyProperties(dbVideo, cacheVideoInfoDto);

        // 从UserInfo缓存对象拿nick
        cacheVideoInfoDto.setNick(
                userInfoCacheManager.getUserInfo(dbVideo.getUserId()).getNick()
        );
        return cacheVideoInfoDto;
    }

    /**
     * 清除视频信息的缓存
     */
//    @CacheEvict(cacheManager = CacheConstant.REMOTE,
//            value = CacheConstant.VIDEO_INFO)
    @CacheInvalidate(name = CacheConstant.VIDEO_INFO, key = "#videoId")
    public void evictVideoInfoCache(Long videoId) {
        // none
        log.debug("视频缓存信息已删除~");
    }
}
