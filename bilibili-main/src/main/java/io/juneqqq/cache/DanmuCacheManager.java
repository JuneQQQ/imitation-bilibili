package io.juneqqq.cache;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.juneqqq.constant.CacheConstant;
import io.juneqqq.dao.entity.Danmu;
import io.juneqqq.dao.mapper.DanmuMapper;
import io.juneqqq.pojo.dto.cache.CacheDanmuDto;
import io.juneqqq.pojo.dto.cache.CacheUserInfoDto;
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
    @Cacheable(cacheManager = CacheConstant.CACHE_TYPE_REDIS,
            value = CacheConstant.DANMU_CACHE_NAME)
    public List<CacheDanmuDto> getDanmuList(Long videoId, String start, String end) {
        var wrapper = new LambdaQueryWrapper<>(Danmu.class)
                .eq(Danmu::getVideoId, videoId);
        if (StringUtil.isNotBlank(start) &&
                StringUtil.isNotBlank(end)) {
            wrapper.between(Danmu::getTimestamp,start,end);
        }
        List<Danmu> danmus = danmuMapper.selectList(wrapper);
        var result = new ArrayList<CacheDanmuDto>();
        for (Danmu danmu : danmus) {
            CacheDanmuDto cacheDanmuDto = new CacheDanmuDto();
            BeanUtil.copyProperties(danmu,cacheDanmuDto);
            result.add(cacheDanmuDto);
        }
        log.debug("本次查询取自数据库，内容：{}", result);
        return result;
    }

    @CacheEvict(cacheManager = CacheConstant.CACHE_TYPE_REDIS,
            value = CacheConstant.DANMU_CACHE_NAME,
            allEntries = true,
            beforeInvocation = true)
    public void evictDanmuCacheAll() {
        log.debug("所有弹幕缓存信息已删除~");
    }
}
