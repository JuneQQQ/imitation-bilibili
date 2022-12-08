package io.juneqqq.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.juneqqq.dao.entity.Danmu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DanmuMapper extends BaseMapper<Danmu> {

    Integer addDanmu(Danmu danmu);

    List<Danmu> getDanmus(Map<String,Object> params);
}
