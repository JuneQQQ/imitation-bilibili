package io.juneqqq.controller;


import io.juneqqq.core.auth.auth.ApiRouterConstant;
import io.juneqqq.core.auth.auth.UserHolder;
import io.juneqqq.dao.entity.Danmu;
import io.juneqqq.dao.entity.R;
import io.juneqqq.service.common.DanmuService;
import io.juneqqq.util.UserSupport;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "DanmuController", description = "弹幕模块")
@RequestMapping(ApiRouterConstant.API_FRONT_DANMU_URL_PREFIX)
public class DanmuController {

    @Resource
    private DanmuService danmuService;

    @GetMapping("/danmus")
    public R<List<Danmu>> getDanmus(@RequestParam Long videoId,
                                    String startTime,
                                    String endTime) {


        List<Danmu> list;
        //判断当前是游客模式还是用户登录模式
        Long userId = UserHolder.getUserId();
        if (userId != null) {
            //若是用户登录模式，则允许用户进行时间段筛选
            list = danmuService.getDanmus(videoId, startTime, endTime);
        } else {
            //若为游客模式，则不允许用户进行时间段筛选
            list = danmuService.getDanmus(videoId, null, null);
        }


        return R.ok(list);
    }
}
