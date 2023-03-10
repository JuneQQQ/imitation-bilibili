package io.juneqqq.pojo.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.juneqqq.pojo.dao.entity.UserMoment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMomentsMapper extends BaseMapper<UserMoment> {

    Integer addUserMoments(UserMoment userMoment);
}
