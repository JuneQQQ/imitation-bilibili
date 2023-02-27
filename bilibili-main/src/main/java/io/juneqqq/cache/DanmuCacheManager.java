package io.juneqqq.cache;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.juneqqq.pojo.dao.entity.Danmu;
import io.juneqqq.pojo.dao.mapper.DanmuMapper;
import io.juneqqq.pojo.dto.cache.CacheDanmuDto;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 弹幕信息 缓存管理类
 */
@Component
@Slf4j
public class DanmuCacheManager {
    @Resource
    DanmuMapper danmuMapper;

    /**
     * 查询弹幕信息，并放入缓存中
     * unless = "#result == null"  不缓存空值
     */
//    @Cacheable(cacheManager = CacheConstant.REMOTE,
//            cacheNames = CacheConstantZ.DANMU)
    @Cached(name = CacheConstant.DANMU, expire = 3600, cacheType = CacheType.REMOTE, key = "#videoId")
    public List<CacheDanmuDto> getDanmuList(Long videoId, String start, String end) {
        var wrapper = new LambdaQueryWrapper<>(Danmu.class)
                .eq(Danmu::getVideoId, videoId);
        if (StringUtil.isNotBlank(start) &&
                StringUtil.isNotBlank(end)) {
            wrapper.between(Danmu::getTimestamp, start, end);
        }
        List<Danmu> danmus = danmuMapper.selectList(wrapper);
        var result = new ArrayList<CacheDanmuDto>();
        for (Danmu danmu : danmus) {
            CacheDanmuDto cacheDanmuDto = new CacheDanmuDto();
            BeanUtil.copyProperties(danmu, cacheDanmuDto);
            result.add(cacheDanmuDto);
        }
        log.debug("本次查询取自数据库，内容：{}", result);
        return result;
    }

    //    @CacheEvict(cacheManager = CacheConstant.REMOTE,
//            value = CacheConstant.DANMU,
//            allEntries = true
//    )
    @CacheInvalidate(name = CacheConstant.DANMU, key = "#danmuId")
    public void evictDanmuCacheAll(Long danmuId) {
        log.debug("所有弹幕缓存信息已删除~");
    }
}
