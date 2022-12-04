package com.juneqqq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juneqqq.entity.dao.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoDao extends BaseMapper<UserInfo> {
}
