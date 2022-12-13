package io.juneqqq.service.common.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.juneqqq.dao.mapper.DanmuMapper;
import io.juneqqq.dao.entity.Danmu;
import io.juneqqq.constant.CacheConstant;
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
    private StringRedisTemplate stringRedisTemplate;

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

        String key = CacheConstant.DANMU_CACHE_NAME + videoId;
        String value = stringRedisTemplate.opsForValue().get(key);
        List<Danmu> list;
        // 缓存有数据
        if (!StringUtil.isNullOrEmpty(value)) {
            list = JSONArray.parseArray(value, Danmu.class);
            if (!StringUtil.isNullOrEmpty(startTime)
                    && !StringUtil.isNullOrEmpty(endTime)) {

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime startDate = LocalDateTime.parse(startTime, dtf);
                LocalDateTime endDate = LocalDateTime.parse(endTime, dtf);
                List<Danmu> childList = new ArrayList<>();
                for (Danmu danmu : list) {
                    LocalDateTime createTime = danmu.getCreateTime();
                    if (createTime.isAfter(startDate) && createTime.isBefore(endDate)) {
                        childList.add(danmu);
                    }
                }
                list = childList;
            }
        } else {
            // 缓存没数据，查库！
            Map<String, Object> params = new HashMap<>();
            params.put("videoId", videoId);
            params.put("startTime", startTime);
            params.put("endTime", endTime);
            list = danmuMapper.getDanmus(params);
            // 存 redis
            stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));
        }
        return list;
    }

    public void addDanmusToRedis(Danmu danmu) {
        String key = CacheConstant.DANMU_CACHE_NAME + danmu.getVideoId();
        String value = stringRedisTemplate.opsForValue().get(key);
        List<Danmu> list = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(value)) {
            list = JSONArray.parseArray(value, Danmu.class);
        }
        list.add(danmu);
        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));
    }

}
