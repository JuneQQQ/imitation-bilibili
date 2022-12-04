package com.juneqqq.controller;


import com.juneqqq.entity.dao.R;
import com.juneqqq.entity.dao.UserMoment;
import com.juneqqq.entity.annotation.ApiRoleLimit;
import com.juneqqq.entity.constant.AuthRoleConstant;
import com.juneqqq.service.common.UserMomentsService;

import com.juneqqq.util.UserSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@RestController
public class UserMomentsApi {

    @Resource
    private UserMomentsService userMomentsService;

    @Resource
    private UserSupport userSupport;


    @ApiRoleLimit(deny = {AuthRoleConstant.ROLE_LV1})
//    @ApiDataLimit
    @PostMapping("/user-moments")
    public R<String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return R.success();
    }

    @GetMapping("/user-subscribed-moments")
    public R<Set<UserMoment>> getUserSubscribedMoments() {
        Long userId = userSupport.getCurrentUserId();
        Set<UserMoment> set = userMomentsService.getUserSubscribedMoments(userId);
        return new R<>(set);
    }

    @GetMapping("/user-moments")
    public R<List<UserMoment>> getUserMoments() {
        Long userId = userSupport.getCurrentUserId();
        List<UserMoment> userMoments = userMomentsService.getUserMoments(userId);
        return new R<>(userMoments);
    }

}
