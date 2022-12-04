package com.juneqqq.service.common;

import com.juneqqq.dao.FollowingGroupDao;
import com.juneqqq.entity.dao.FollowingGroup;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FollowingGroupService {

    @Resource
    private FollowingGroupDao followingGroupDao;

    public FollowingGroup getByType(String type) {
        return followingGroupDao.getByType(type);
//        return followingGroupDao.selectOne(new LambdaQueryWrapper<FollowingGroup>().
//                eq(FollowingGroup::getType, type));
    }

    public FollowingGroup getById(Long id) {
        return followingGroupDao.selectById(id);
//        return followingGroupDao.getById(id);
    }

    public List<FollowingGroup> getByUserId(Long userId) {
        return followingGroupDao.getByUserId(userId);
//        return followingGroupDao.selectList(new LambdaQueryWrapper<FollowingGroup>().
//                eq(FollowingGroup::getUserId, userId).or().in(FollowingGroup::getUserId, List.of(1, 2, 3)));
    }

    public void addFollowingGroup(FollowingGroup followingGroup) {
//        followingGroupDao.addFollowingGroup(followingGroup);
        followingGroupDao.insert(followingGroup);
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupDao.getUserFollowingGroups(userId);
//        return followingGroupDao.selectList(new LambdaQueryWrapper<FollowingGroup>().
//                eq(FollowingGroup::getUserId,userId));
    }
}
