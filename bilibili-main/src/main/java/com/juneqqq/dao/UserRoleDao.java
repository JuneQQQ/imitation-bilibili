package com.juneqqq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juneqqq.entity.auth.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserRoleDao extends BaseMapper<UserRole> {

    List<UserRole> getUserRoleByUserId(Long userId);

    Integer addUserRole(UserRole userRole);
}
