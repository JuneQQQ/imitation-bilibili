package io.juneqqq.service.common;

import io.juneqqq.dao.entity.FollowingGroup;
import io.juneqqq.dao.entity.UserFollowing;
import io.juneqqq.dao.entity.UserInfo;

import java.util.List;

public interface UserFollowingService {
    boolean getFollowingRelation(long cId, long fId);
    void addUserFollowings(UserFollowing userFollowing);


    List<FollowingGroup> getUserFollowings(Long userId);

    List<UserFollowing> getUserFans(Long userId);


    Long addUserFollowingGroups(FollowingGroup followingGroup);


    List<FollowingGroup> getUserFollowingGroups(Long userId);

    List<UserInfo> checkFollowingStatus(List<UserInfo> userInfoList, Long userId);
}
