package com.juneqqq.controller;


import com.juneqqq.entity.dao.FollowingGroup;
import com.juneqqq.entity.dao.R;
import com.juneqqq.entity.dao.UserFollowing;
import com.juneqqq.service.common.UserFollowingService;

import com.juneqqq.util.UserSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class UserFollowingApi {

    @Resource
    private UserFollowingService userFollowingService;

    @Resource
    private UserSupport userSupport;

    /**
     * 添加关注用户
     */
    @PostMapping("/user-followings")
    public R<String> addUserFollowings(@RequestBody UserFollowing userFollowing){
        Long userId = userSupport.getCurrentUserId();
        userFollowing.setUserId(userId);
        userFollowingService.addUserFollowings(userFollowing);
        return R.success();
    }

    /**
     * 获取当前用户关注了谁
     */
    @GetMapping("/user-followings")
    public R<List<FollowingGroup>> getUserFollowings(){
        Long userId = userSupport.getCurrentUserId();
        List<FollowingGroup> result = userFollowingService.getUserFollowings(userId);
        return new R<>(result);
    }

    /**
     * 获取当前用户被谁关注
     */
    @GetMapping("/user-fans")
    public R<List<UserFollowing>> getUserFans(){
        Long userId = userSupport.getCurrentUserId();
        List<UserFollowing> result = userFollowingService.getUserFans(userId);
        return new R<>(result);
    }

    /**
     * 添加关注用户的分组
     */
    @PostMapping("/user-following-groups")
    public R<Long> addUserFollowingGroups(@RequestBody FollowingGroup followingGroup){
        Long userId = userSupport.getCurrentUserId();
        followingGroup.setUserId(userId);
        Long groupId = userFollowingService.addUserFollowingGroups(followingGroup);
        return new R<>(groupId);
    }

    /**
     * 获取关注用户的分组
     */
    @GetMapping("/user-following-groups")
    public R<List<FollowingGroup>> getUserFollowingGroups(){
        Long userId = userSupport.getCurrentUserId();
        List<FollowingGroup> list = userFollowingService.getUserFollowingGroups(userId);
        return new R<>(list);
    }

}
