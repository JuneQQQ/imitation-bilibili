package io.juneqqq.controller;


import io.juneqqq.core.auth.auth.ApiRouterConstant;
import io.juneqqq.pojo.dao.entity.Danmu;
import io.juneqqq.pojo.dao.entity.R;
import io.juneqqq.service.common.DanmuService;
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

        List<Danmu> list = danmuService.getDanmus(videoId, startTime, endTime);
        return R.ok(list);
    }
}
