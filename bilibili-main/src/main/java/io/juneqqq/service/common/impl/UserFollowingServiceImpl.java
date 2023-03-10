package io.juneqqq.service.common.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.core.exception.ErrorCodeEnum;
import io.juneqqq.pojo.dao.mapper.UserFollowingMapper;
import io.juneqqq.constant.UserConstant;
import io.juneqqq.pojo.dao.entity.FollowingGroup;
import io.juneqqq.pojo.dao.entity.User;
import io.juneqqq.pojo.dao.entity.UserFollowing;
import io.juneqqq.pojo.dao.entity.UserInfo;
import io.juneqqq.pojo.dao.repository.UserInfoDtoRepository;
import io.juneqqq.pojo.dao.repository.esmodel.EsUserInfoDto;
import io.juneqqq.service.common.FollowingGroupService;
import io.juneqqq.service.common.UserFollowingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingServiceImpl implements UserFollowingService {

    @Resource
    private UserFollowingMapper userFollowingMapper;

    @Resource
    private UserInfoDtoRepository userInfoDtoRepository;
    @Resource
    private FollowingGroupService followingGroupService;
    @Resource
    private UserServiceImpl userService;
    public boolean getFollowingRelation(long cId, long fId) {
        return userFollowingMapper.selectOne(new LambdaQueryWrapper<>(UserFollowing.class)
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
                throw new BusinessException(ErrorCodeEnum.TARGET_GROUP_NOT_EXISTS);
            }
        }
        Long followingId = userFollowing.getFollowingId();
        User user = userService.getUserById(followingId);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.TARGET_USER_NOT_EXISTS);
        }
        // ?????????????????????????????????
        userFollowingMapper.delete(new LambdaQueryWrapper<UserFollowing>().eq(UserFollowing::getFollowingId, followingId));
        userFollowingMapper.insert(userFollowing);

        UserInfo userInfo = userService.getUserInfo(user.getId());
        userInfo.setFollowing(true);
        userInfoDtoRepository.deleteByUserId(userInfo.getUserId());
        EsUserInfoDto es = userService.getEsUserInfoDto(userInfo.getUserId());
        userInfoDtoRepository.save(es);
    }


    /**
     * ?????????????????? following??????????????????
     */
    public List<FollowingGroup> getUserFollowings(Long userId) {
        // 1.??????????????????
        List<UserFollowing> list = userFollowingMapper.selectList(new LambdaQueryWrapper<UserFollowing>().
                eq(UserFollowing::getUserId, userId)); // ?????????????????? ?????????????????? ??????
        // ????????????????????????id
        Set<Long> followingIdSet = list.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());
        List<UserInfo> userInfoList = new ArrayList<>();
        if (followingIdSet.size() > 0) {
            // ????????????????????????????????????
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);
        }

        // 2.???????????????following???userinfo
        // ??????????????????
        for (UserFollowing userFollowing : list) {
            // ????????????????????????????????????
            for (UserInfo userInfo : userInfoList) {
                // ?????????????????????????????????????????? ???????????? ???????????? userInfo ???
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }

        // 3.???????????????
        List<FollowingGroup> result = new ArrayList<>();
        List<FollowingGroup> groupList = followingGroupService.getByUserId(userId);
        // 3.1 ?????????????????????????????????????????????
        // allGroup->???????????????????????????
        FollowingGroup allGroup = new FollowingGroup();
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        allGroup.setFollowingUserInfoList(userInfoList);
        result.add(allGroup);

        // 3.2 ????????????3??????????????????????????????info
        // ?????????????????????????????????
        for (FollowingGroup curGroup : groupList) {
            // 3.2.1 ??????????????????????????????
            List<UserInfo> curGroupFollowingList = new ArrayList<>();
            // ???????????????????????????????????????
            for (UserFollowing userFollowing : list) {
                if (curGroup.getId().equals(userFollowing.getGroupId())) {
                    // ???????????????????????????
                    curGroupFollowingList.add(userFollowing.getUserInfo());
                }
            }
            // 3.2.2 ????????????????????????->List<UserInfo>
            curGroup.setFollowingUserInfoList(curGroupFollowingList);
            result.add(curGroup);
        }
        return result;
    }

    /**
     * ?????????????????? ??????
     */
    public List<UserFollowing> getUserFanInfos(Long userId) {
        // ????????????????????? ?????????????????? ??????
        List<UserFollowing> relations = userFollowingMapper.selectList(
                new LambdaQueryWrapper<UserFollowing>().
                        eq(UserFollowing::getFollowingId, userId));
        // ????????????id??????
        Set<Long> fanIdSet = relations.stream().
                map(UserFollowing::getUserId).collect(Collectors.toSet());
        // ??????info??????
        List<UserInfo> fansInfoList = new ArrayList<>();
        if (fanIdSet.size() > 0) {
            // ????????????info??????
            fansInfoList = userService.getUserInfoByUserIds(fanIdSet);
        }
        // ??????????????????????????????
        List<UserFollowing> followingList = userFollowingMapper.selectList(
                new LambdaQueryWrapper<UserFollowing>().eq(UserFollowing::getUserId, userId));
        // ?????????????????????????????????????????????userInfo
        for (UserFollowing relation : relations) {
            // ????????????Info
            for (UserInfo fanInfo : fansInfoList) {
                // ??????id = ????????????userId
                if (relation.getUserId().equals(fanInfo.getUserId())) {
                    // ??????info????????????????????????user following
                    relation.setUserInfo(fanInfo);
                }
            }
            // ????????????????????????
            for (UserFollowing following : followingList) {
                if (following.getFollowingId().equals(relation.getUserId())) {
                    // ????????????????????????
                    relation.getUserInfo().setFollowing(true);
                }
            }
        }
        return relations;
    }

    @Override
    public Integer getUserFanCount(Long userId) {
        return Math.toIntExact(userFollowingMapper.selectCount(new LambdaQueryWrapper<>(UserFollowing.class)
                .eq(UserFollowing::getFollowingId, userId)));
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
        List<UserFollowing> userFollowingList = userFollowingMapper.selectList(
                new LambdaQueryWrapper<UserFollowing>().eq(UserFollowing::getUserId, userId)
        );  // ?????????(userId)?????????????????????
        // ???userInfoList????????????????????????
        for (UserInfo userInfo : userInfoList) {
            for (UserFollowing userFollowing : userFollowingList) {
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    // ???????????????????????????????????????????????????????????????
                    userInfo.setFollowing(true);
                }
            }
        }
        return userInfoList;
    }
}
