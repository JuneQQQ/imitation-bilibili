package com.juneqqq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juneqqq.entity.dao.UserMoment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMomentsDao extends BaseMapper<UserMoment> {

    Integer addUserMoments(UserMoment userMoment);
}
