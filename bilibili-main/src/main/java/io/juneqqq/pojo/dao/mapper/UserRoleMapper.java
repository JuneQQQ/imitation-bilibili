package io.juneqqq.pojo.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.juneqqq.core.auth.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    List<UserRole> getUserRoleByUserId(Long userId);

    Integer addUserRole(UserRole userRole);
}
