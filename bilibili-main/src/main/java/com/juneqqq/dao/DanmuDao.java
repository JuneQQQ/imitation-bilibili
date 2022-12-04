package com.juneqqq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juneqqq.entity.dao.Danmu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DanmuDao extends BaseMapper<Danmu> {

    Integer addDanmu(Danmu danmu);

    List<Danmu> getDanmus(Map<String,Object> params);
}
