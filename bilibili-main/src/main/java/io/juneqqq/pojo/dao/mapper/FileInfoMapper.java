package io.juneqqq.pojo.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.juneqqq.pojo.dao.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {

    Integer addFile(FileInfo fileInfo);

    FileInfo getFileByMD5(String md5);
}
