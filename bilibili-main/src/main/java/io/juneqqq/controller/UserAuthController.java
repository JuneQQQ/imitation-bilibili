package io.juneqqq.controller;


import io.juneqqq.core.auth.auth.UserHolder;
import io.juneqqq.dao.entity.R;
import io.juneqqq.core.auth.UserAuthorities;
import io.juneqqq.service.common.UserAuthService;
import io.juneqqq.util.UserSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
public class UserAuthController {

    @Resource
    private UserSupport userSupport;

    @Resource
    private UserAuthService userAuthService;

    @GetMapping("/user-authorities")
    public R<UserAuthorities> getUserAuthorities(){
        UserAuthorities userAuthorities = userAuthService.getUserAuthorities(UserHolder.getUserId());
        return R.ok(userAuthorities);
    }
}
