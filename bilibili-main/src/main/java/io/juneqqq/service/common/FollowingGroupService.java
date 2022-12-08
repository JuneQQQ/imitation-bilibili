package io.juneqqq.service.common;

import io.juneqqq.dao.entity.FollowingGroup;

import java.util.List;

public interface FollowingGroupService {
    FollowingGroup getByType(String type);
    FollowingGroup getById(Long id);
    List<FollowingGroup> getByUserId(Long userId);
    void addFollowingGroup(FollowingGroup followingGroup);
    List<FollowingGroup> getUserFollowingGroups(Long userId);
}
