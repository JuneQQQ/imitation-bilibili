package com.juneqqq.service.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.juneqqq.dao.UserFollowingDao;
import com.juneqqq.entity.constant.UserConstant;
import com.juneqqq.entity.dao.FollowingGroup;
import com.juneqqq.entity.dao.User;
import com.juneqqq.entity.dao.UserFollowing;
import com.juneqqq.entity.dao.UserInfo;
import com.juneqqq.entity.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingService {

    @Resource
    private UserFollowingDao userFollowingDao;

    @Resource
    private FollowingGroupService followingGroupService;


    @Resource
    private ElasticSearchService elasticSearchService;

    @Resource
    private UserService userService;


    public boolean getFollowingRelation(long cId, long fId) {
        return userFollowingDao.selectOne(new LambdaQueryWrapper<>(UserFollowing.class)
                .eq(UserFollowing::getFollowingId, fId)
                .eq(UserFollowing::getUserId, cId)) != null;
    }

    @Transactional
    public void addUserFollowings(UserFollowing userFollowing) {
        Long groupId = userFollowing.getGroupId();
        if (groupId == null) {
            FollowingGroup followingGroup = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setGroupId(followingGroup.getId());
        } else {
            FollowingGroup followingGroup = followingGroupService.getById(groupId);
            if (followingGroup == null) {
                throw new CustomException("关注分组不存在！");
            }
        }
        Long followingId = userFollowing.getFollowingId();
        User user = userService.getUserById(followingId);
        if (user == null) {
            throw new CustomException("关注的用户不存在！");
        }
        // 添加关联关系：先删再加
        userFollowingDao.delete(new LambdaQueryWrapper<UserFollowing>().eq(UserFollowing::getFollowingId, followingId));
        userFollowingDao.insert(userFollowing);

//        UserInfo userInfo = userService.getUserInfo(user.getId());
//        userInfo.setFollowing(true);
//        elasticSearchService.deleteUserInfo(userInfo);
//        elasticSearchService.addUserInfo(userInfo);
    }


    /**
     * 获取当前用户 following（关注了谁）
     */
    public List<FollowingGroup> getUserFollowings(Long userId) {
        // 1.查询所需数据
        List<UserFollowing> list = userFollowingDao.selectList(new LambdaQueryWrapper<UserFollowing>().
                eq(UserFollowing::getUserId, userId)); // 查询当前用户 粉丝关联关系 集合
        // 收集我关注的人的id
        Set<Long> followingIdSet = list.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());
        List<UserInfo> userInfoList = new ArrayList<>();
        if (followingIdSet.size() > 0) {
            // 获取我关注的人的详细信息
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);
        }

        // 2.先设置每个following的userinfo
        // 遍历关联关系
        for (UserFollowing userFollowing : list) {
            // 遍历我关注的人的详细信息
            for (UserInfo userInfo : userInfoList) {
                // 将我关注的人的详细信息设置到 关联关系 冗余字段 userInfo 中
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }

        // 3.再设置分组
        List<FollowingGroup> result = new ArrayList<>();
        List<FollowingGroup> groupList = followingGroupService.getByUserId(userId);
        // 3.1 获取当前用户关注的人的所有分组
        // allGroup->所有分组的数据汇总
        FollowingGroup allGroup = new FollowingGroup();
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        allGroup.setFollowingUserInfoList(userInfoList);
        result.add(allGroup);

        // 3.2 收集其他3个分组对应的关注用户info
        // 遍历用户拥有所有的分组
        for (FollowingGroup curGroup : groupList) {
            // 3.2.1 得到当前分组用户集合
            List<UserInfo> curGroupFollowingList = new ArrayList<>();
            // 遍历所有的用户关注关联关系
            for (UserFollowing userFollowing : list) {
                if (curGroup.getId().equals(userFollowing.getGroupId())) {
                    // 属于该分组中的数据
                    curGroupFollowingList.add(userFollowing.getUserInfo());
                }
            }
            // 3.2.2 设置当前分组属性->List<UserInfo>
            curGroup.setFollowingUserInfoList(curGroupFollowingList);
            result.add(curGroup);
        }
        return result;
    }

    /**
     * 获取当前用户 粉丝
     */
    public List<UserFollowing> getUserFans(Long userId) {
        // 查询当前用户的 粉丝关联关系 集合
        List<UserFollowing> relations = userFollowingDao.selectList(
                new LambdaQueryWrapper<UserFollowing>().
                        eq(UserFollowing::getFollowingId, userId));
        // 抽取粉丝id集合
        Set<Long> fanIdSet = relations.stream().
                map(UserFollowing::getUserId).collect(Collectors.toSet());
        // 粉丝info集合
        List<UserInfo> fansInfoList = new ArrayList<>();
        if (fanIdSet.size() > 0) {
            // 获取粉丝info集合
            fansInfoList = userService.getUserInfoByUserIds(fanIdSet);
        }
        // 获取当前用户关注了谁
        List<UserFollowing> followingList = userFollowingDao.selectList(
                new LambdaQueryWrapper<UserFollowing>().eq(UserFollowing::getUserId, userId));
        // 遍历粉丝的关联关系，设置其字段userInfo
        for (UserFollowing relation : relations) {
            // 遍历粉丝Info
            for (UserInfo fanInfo : fansInfoList) {
                // 粉丝id = 关联关系userId
                if (relation.getUserId().equals(fanInfo.getUserId())) {
                    // 粉丝info作为冗余字段加入user following
                    relation.setUserInfo(fanInfo);
                }
            }
            // 遍历我的关注列表
            for (UserFollowing following : followingList) {
                if (following.getFollowingId().equals(relation.getUserId())) {
                    // 我也关注了该粉丝
                    relation.getUserInfo().setFollowing(true);
                }
            }
        }
        return relations;
    }

    public Long addUserFollowingGroups(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(LocalDateTime.now());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);
        followingGroupService.addFollowingGroup(followingGroup);
        return followingGroup.getId();
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getUserFollowingGroups(userId);
    }

    public List<UserInfo> checkFollowingStatus(List<UserInfo> userInfoList, Long userId) {
//        List<UserFollowing> userFollowingList = userFollowingDao.getUserFollowings(userId);
        List<UserFollowing> userFollowingList = userFollowingDao.selectList(
                new LambdaQueryWrapper<UserFollowing>().eq(UserFollowing::getUserId, userId)
        );  // 查询我(userId)关注了哪些用户
        // 看userInfoList中哪些是我关注的
        for (UserInfo userInfo : userInfoList) {
            for (UserFollowing userFollowing : userFollowingList) {
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    // 外层用户是否关注了此用户（我关注了此用户）
                    userInfo.setFollowing(true);
                }
            }
        }
        return userInfoList;
    }
}
