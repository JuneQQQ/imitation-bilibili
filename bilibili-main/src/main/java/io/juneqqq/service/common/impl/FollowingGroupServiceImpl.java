package io.juneqqq.service.common.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.juneqqq.pojo.dao.mapper.FollowingGroupMapper;
import io.juneqqq.pojo.dao.entity.FollowingGroup;
import io.juneqqq.service.common.FollowingGroupService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class FollowingGroupServiceImpl implements FollowingGroupService {

    @Resource
    private FollowingGroupMapper followingGroupMapper;

    public FollowingGroup getByType(String type) {
        return followingGroupMapper.selectOne(new LambdaQueryWrapper<FollowingGroup>().
                eq(FollowingGroup::getType, type));
    }

    public FollowingGroup getById(Long id) {
        return followingGroupMapper.selectById(id);
    }

    public List<FollowingGroup> getByUserId(Long userId) {
        return followingGroupMapper.getByUserId(userId);
    }

    public void addFollowingGroup(FollowingGroup followingGroup) {
        followingGroupMapper.insert(followingGroup);
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupMapper.selectList(new LambdaQueryWrapper<FollowingGroup>().
                eq(FollowingGroup::getUserId,userId));
    }
}
