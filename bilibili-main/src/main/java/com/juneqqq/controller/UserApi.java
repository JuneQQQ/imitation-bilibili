package com.juneqqq.controller;

import com.alibaba.fastjson2.JSONObject;
import com.juneqqq.entity.dao.PageResult;
import com.juneqqq.entity.dao.R;
import com.juneqqq.entity.dao.User;
import com.juneqqq.entity.dao.UserInfo;
import com.juneqqq.service.common.UserFollowingService;
import com.juneqqq.service.common.UserService;
import com.juneqqq.util.RSAUtil;
import com.juneqqq.util.UserSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class UserApi {

    @Resource
    private UserService userService;

    @Resource
    private UserSupport userSupport;

    @Resource
    private UserFollowingService userFollowingService;

    @GetMapping("/users")
    public R<User> getUserInfo() {
        Long userId = userSupport.getCurrentUserId();
        User user = userService.getUser(userId);
        return new R<>(user);
    }

    @GetMapping("/rsa-pks")
    public R<String> getRsaPublicKey() {
        String pk = RSAUtil.getPublicKeyStr();
        return new R<>(pk);
    }

    /**
     * 添加/注册用户
     */
    @PostMapping("/users")
    public R<String> addUser(@RequestBody User user) {
        userService.addUser(user);
        return R.success();
    }

    @PostMapping("/user-tokens")
    public R<String> login(@RequestBody User user) throws Exception {
        String token = userService.login(user);
        return new R<>(token);
    }

    /**
     * 修改用户信息
     */
    @PutMapping("/users")
    public R<String> updateUsers(@RequestBody User user) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        user.setId(userId);
        userService.updateUsers(user);
        return R.success();
    }


    /**
     * 修改用户info
     */
    @PutMapping("/user-infos")
    public R<String> updateUserInfos(@RequestBody UserInfo userInfo) {
        Long userId = userSupport.getCurrentUserId();
        userInfo.setUserId(userId);
        userService.updateUserInfos(userInfo);
        return R.success();
    }

    /**
     * 查询用户info
     */
    @GetMapping("/user-infos")
    public R<PageResult<UserInfo>> pageListUserInfos(
            Long no,
            Long size,
            @RequestParam(required = false) String nick) {
        Long userId = userSupport.getCurrentUserId();
        JSONObject params = new JSONObject();
        params.put("no", no);
        params.put("size", size);
        params.put("nick", nick);
        params.put("userId", userId);

        log.debug("查询用户info服务端接收到的参数：" + params);

        PageResult<UserInfo> result = userService.pageListUserInfos(params);

        log.debug(result.toString());
        if (result.getTotal() > 0) {
            // 检查这些查出来的info是否关注了自己 -> followed=true
            userFollowingService.checkFollowingStatus(result.getList(), userId);
//            result.setList(checkedUserInfoList);
        }
        log.debug(result.toString());
        return new R<>(result);
    }

    /**
     * 此方法仍然返回token(accessToken)
     * refreshToken是保存在 redis 中的
     */
    @PostMapping("/user-dts")
    public R<String> loginForDts(@RequestBody User user) throws Exception {
        return new R<>(userService.loginForDts(user));
    }

    @DeleteMapping("/refresh-tokens")
    public R<String> logout(HttpServletRequest request) {
        userSupport.deleteToken();
        return R.success();
    }

    /**
     * 前端主动更新refreshToken
     */
    @PostMapping("/access-tokens")
    public R<String> refreshAccessToken(Long userId) throws Exception {
        return new R<>(userSupport.refreshToken(userId));
    }

}
