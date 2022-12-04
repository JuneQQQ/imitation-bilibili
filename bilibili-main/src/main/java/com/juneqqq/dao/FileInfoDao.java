package com.juneqqq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juneqqq.entity.dao.FileInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileInfoDao extends BaseMapper<FileInfo> {

    Integer addFile(FileInfo fileInfo);

    FileInfo getFileByMD5(String md5);
}
