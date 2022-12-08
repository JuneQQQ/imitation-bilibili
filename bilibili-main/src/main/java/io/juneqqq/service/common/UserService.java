package io.juneqqq.service.common;

import com.alibaba.fastjson2.JSONObject;
import io.juneqqq.core.entity.PageResult;
import io.juneqqq.dao.entity.User;
import io.juneqqq.dao.entity.UserInfo;

import java.util.List;
import java.util.Set;

public interface UserService {
    Integer getCoinAmount(Long userId);

    String getRefreshTokenByUserId(Long userId);


    List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList);

    String loginForDts(User user);


    PageResult<UserInfo> pageListUserInfos(JSONObject params);

    List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList);
    User getUserById(Long followingId);

    void updateUserInfos(UserInfo userInfo);

    void updateUsers(User user);

    User getUser(Long userId);

    String login(User user);

    User getUserByPhone(String phone);

    void updateCoin(Long userId, Integer target);

    void addUser(User user);


    UserInfo getUserInfo(Long userId);
}
