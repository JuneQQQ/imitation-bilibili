package com.juneqqq.service.common;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.juneqqq.dao.DanmuDao;
import com.juneqqq.entity.dao.Danmu;
import io.netty.util.internal.StringUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.juneqqq.entity.constant.RedisPrefix.DANMU_KEY;

@Service
public class DanmuService {


    @Resource
    private DanmuDao danmuDao;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void addDanmu(Danmu danmu) {
        danmuDao.addDanmu(danmu);
    }

    @Async
    public void asyncAddDanmu(Danmu danmu) {
        danmuDao.insert(danmu);
    }

    /**
     * 查询策略是优先查redis中的弹幕数据，
     * 如果没有的话查询数据库，然后把查询的数据写入redis当中
     */
    public List<Danmu> getDanmus(Long videoId,
                                 String startTime, String endTime) throws Exception {

        String key = DANMU_KEY + videoId;
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
            list = danmuDao.getDanmus(params);
            // 存 redis
            stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));
        }
        return list;
    }

    public void addDanmusToRedis(Danmu danmu) {
        String key = DANMU_KEY + danmu.getVideoId();
        String value = stringRedisTemplate.opsForValue().get(key);
        List<Danmu> list = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(value)) {
            list = JSONArray.parseArray(value, Danmu.class);
        }
        list.add(danmu);
        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));
    }

}
