package io.juneqqq.service.common.impl;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.juneqqq.cache.DanmuCacheManager;
import io.juneqqq.dao.mapper.DanmuMapper;
import io.juneqqq.dao.entity.Danmu;
import io.juneqqq.constant.CacheConstant;
import io.juneqqq.pojo.dto.cache.CacheDanmuDto;
import io.juneqqq.service.common.DanmuService;
import io.netty.util.internal.StringUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DanmuServiceImpl implements DanmuService {
    @Resource
    private DanmuMapper danmuMapper;

    @Resource
    private DanmuCacheManager danmuCacheManager;

    public void addDanmu(Danmu danmu) {
        danmuMapper.addDanmu(danmu);
    }

    @Async
    public void asyncAddDanmu(Danmu danmu) {
        danmuMapper.insert(danmu);
    }

    /**
     * 查询策略是优先查redis中的弹幕数据，
     * 如果没有的话查询数据库，然后把查询的数据写入redis当中
     */
    public List<Danmu> getDanmus(Long videoId, String startTime, String endTime) {

        List<Danmu> list = new ArrayList<>();
        // 缓存有数据
        List<CacheDanmuDto> danmuList = danmuCacheManager.getDanmuList(videoId, startTime, endTime);
        for (CacheDanmuDto cacheDanmuDto : danmuList) {
            Danmu danmu = new Danmu();
            BeanUtil.copyProperties(cacheDanmuDto,danmu);
            list.add(danmu);
        }
        return list;
    }
}
