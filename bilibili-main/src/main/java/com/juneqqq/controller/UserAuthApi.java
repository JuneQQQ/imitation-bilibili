package com.juneqqq.controller;


import com.juneqqq.entity.dao.R;
import com.juneqqq.entity.auth.UserAuthorities;
import com.juneqqq.service.common.UserAuthService;
import com.juneqqq.util.UserSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class UserAuthApi {

    @Resource
    private UserSupport userSupport;

    @Resource
    private UserAuthService userAuthService;

    @GetMapping("/user-authorities")
    public R<UserAuthorities> getUserAuthorities(){
        Long userId = userSupport.getCurrentUserId();
        UserAuthorities userAuthorities = userAuthService.getUserAuthorities(userId);
        return new R<>(userAuthorities);
    }
}
