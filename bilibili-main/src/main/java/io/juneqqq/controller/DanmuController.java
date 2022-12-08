package io.juneqqq.controller;



import io.juneqqq.dao.entity.Danmu;
import io.juneqqq.dao.entity.R;
import io.juneqqq.service.common.DanmuService;
import io.juneqqq.util.UserSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
public class DanmuController {

    @Resource
    private DanmuService danmuService;

    @Resource
    private UserSupport userSupport;

    @GetMapping("/danmus")
    public R<List<Danmu>> getDanmus(@RequestParam Long videoId,
                                    String startTime,
                                    String endTime) throws Exception {


        List<Danmu> list;
        try{
            //判断当前是游客模式还是用户登录模式
            userSupport.getCurrentUserId();
            //若是用户登录模式，则允许用户进行时间段筛选
            list = danmuService.getDanmus(videoId, startTime, endTime);
        }catch (Exception ignored){
            //若为游客模式，则不允许用户进行时间段筛选
            list = danmuService.getDanmus(videoId, null, null);
        }
        return new R<>(list);
    }

}
