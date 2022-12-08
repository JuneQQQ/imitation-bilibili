package io.juneqqq.service.common;

import io.juneqqq.dao.entity.Danmu;

import java.util.List;

public interface DanmuService {
    void addDanmu(Danmu danmu);
    void asyncAddDanmu(Danmu danmu);
    List<Danmu> getDanmus(Long videoId, String startTime, String endTime);
    void addDanmusToRedis(Danmu danmu);
}
