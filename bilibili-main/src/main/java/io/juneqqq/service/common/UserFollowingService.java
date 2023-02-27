package io.juneqqq.service.common;

import io.juneqqq.pojo.dao.entity.FollowingGroup;
import io.juneqqq.pojo.dao.entity.UserFollowing;
import io.juneqqq.pojo.dao.entity.UserInfo;

import java.util.List;

public interface UserFollowingService {
    boolean getFollowingRelation(long cId, long fId);
    void addUserFollowings(UserFollowing userFollowing);


    List<FollowingGroup> getUserFollowings(Long userId);

    List<UserFollowing> getUserFanInfos(Long userId);

    /**
     * 获取用户粉丝数
     * @param userId 用户id
     * @return Long 数目
     */
    Integer getUserFanCount(Long userId);


    Long addUserFollowingGroups(FollowingGroup followingGroup);


    List<FollowingGroup> getUserFollowingGroups(Long userId);

    List<UserInfo> checkFollowingStatus(List<UserInfo> userInfoList, Long userId);
}
