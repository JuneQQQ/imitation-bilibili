package io.juneqqq.controller;

import io.juneqqq.core.auth.auth.ApiRouterConstant;
import io.juneqqq.core.auth.auth.UserHolder;
import io.juneqqq.pojo.dao.entity.*;
import io.juneqqq.pojo.dto.PageResult;
import io.juneqqq.pojo.dto.request.LoginUserDtoReq;
import io.juneqqq.pojo.dto.response.LoginUserDtoResp;
import io.juneqqq.service.common.UserFollowingService;
import io.juneqqq.service.common.UserMomentService;
import io.juneqqq.service.common.UserService;
import io.juneqqq.util.RSAUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static io.juneqqq.core.auth.auth.SystemConfigConstant.HTTP_AUTH_HEADER_NAME;

@Slf4j
@RestController
@Tag(name = "UserController", description = "用户模块")
@RequestMapping(ApiRouterConstant.API_FRONT_USER_URL_PREFIX)
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private UserMomentService userMomentService;

    @Resource
    private UserFollowingService userFollowingService;

    @GetMapping("/users")
    @Operation(summary = "获取登录用户Info")
    public R<User> getUserInfo() {
        User user = userService.getUser(UserHolder.getUserId());
        return R.ok(user);
    }

    @GetMapping("/rsa-pks")
    public R<String> getRsaPublicKey() {
        String pk = RSAUtil.getPublicKeyStr();
        return R.ok(pk);
    }

    /**
     * 添加/注册用户
     */
    @Operation(summary = "添加/注册用户")
    @PostMapping("/register")
    public R<Void> addUser(@RequestBody User user) {
        userService.addUser(user);
        return R.ok();
    }

    /**
     * 用户登录接口
     */
    @Operation(summary = "用户登录接口")
    @PostMapping("/login")
    public R<LoginUserDtoResp> login(@RequestBody LoginUserDtoReq user) {
        LoginUserDtoResp resp = userService.login(user);
        return R.ok(resp);
    }

    /**
     * 退出登录
     * 仅仅是清除 redis 的刷新标志，该令牌其实仍然有效，在令牌生命周期较短时此方式比较好用
     * 如果想要真正无效，可以设置JWT黑名单
     */
    @Operation(summary = "用户登录接口")
    @RequestMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        userService.logout(UserHolder.getUserId(), request.getHeader(HTTP_AUTH_HEADER_NAME));
        return R.ok();
    }

    /**
     * 修改用户信息
     */
    @PutMapping("/users")
    public R<Void> updateUsers(@RequestBody User user) {
        user.setId(UserHolder.getUserId());
        userService.updateUsers(user);
        return R.ok();
    }


    /**
     * 修改用户info
     */
    @PutMapping("/user-infos")
    public R<Void> updateUserInfos(@RequestBody UserInfo userInfo) {
        userInfo.setUserId(userInfo.getUserId());
        userService.updateUserInfo(userInfo);
        return R.ok();
    }

    /**
     * 根据昵称查询用户info
     */
    @GetMapping("/user-infos")
    public R<PageResult<UserInfo>> pageListUserInfos(
            Long no,
            Long size,
            @RequestParam(required = false) String nick) {

        PageResult<UserInfo> result = userService.pageListUserInfos(no, size, nick, UserHolder.getUserId());
        log.debug(result.toString());
        if (result.total() > 0) {
            // 检查这些查出来的info是否关注了自己 -> followed=true
            userFollowingService.checkFollowingStatus(result.list(), UserHolder.getUserId());
        }
        log.debug(result.toString());
        return R.ok(result);
    }

    /**
     * 添加关注用户
     */
    @PostMapping("/user-followings")
    public R<Void> addUserFollowings(@RequestBody UserFollowing userFollowing) {
        userFollowing.setUserId(UserHolder.getUserId());
        userFollowingService.addUserFollowings(userFollowing);
        return R.ok();
    }

    /**
     * 获取当前用户关注了谁
     */
    @GetMapping("/user-followings")
    public R<List<FollowingGroup>> getUserFollowings() {
        List<FollowingGroup> result = userFollowingService.getUserFollowings(UserHolder.getUserId());
        return R.ok(result);
    }

    /**
     * 获取当前用户被谁关注
     */
    @GetMapping("/user-fans")
    public R<List<UserFollowing>> getUserFans() {
        List<UserFollowing> result = userFollowingService.getUserFanInfos(UserHolder.getUserId());
        return R.ok(result);
    }

    /**
     * 添加关注用户的分组
     */
    @PostMapping("/user-following-groups")
    public R<String> addUserFollowingGroups(@RequestBody FollowingGroup followingGroup) {
        followingGroup.setUserId(UserHolder.getUserId());
        Long groupId = userFollowingService.addUserFollowingGroups(followingGroup);
        return R.ok(String.valueOf(groupId));
    }

    /**
     * 获取关注用户的分组
     */
    @GetMapping("/user-following-groups")
    public R<List<FollowingGroup>> getUserFollowingGroups() {
        List<FollowingGroup> list = userFollowingService.getUserFollowingGroups(UserHolder.getUserId());
        return R.ok(list);
    }


    @PostMapping("/user-moments")
    public R<Void> addUserMoments(@RequestBody UserMoment userMoment) {
        userMoment.setUserId(UserHolder.getUserId());
        userMomentService.addUserMoments(userMoment);
        return R.ok();
    }

    @GetMapping("/user-subscribed-moments")
    public R<Set<UserMoment>> getUserSubscribedMoments() {
        Set<UserMoment> set = userMomentService.getUserSubscribedMoments(UserHolder.getUserId());
        return R.ok(set);
    }

    @GetMapping("/user-moments")
    public R<List<UserMoment>> getUserMoments() {
        List<UserMoment> userMoments = userMomentService.getUserMoments(UserHolder.getUserId());
        return R.ok(userMoments);
    }

}

