package com.juneqqq.entity.vo;

import com.juneqqq.entity.constant.FileStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileStatus {
    private FileStatusEnum status;
    private PartInfo partInfo;

    // 文件已存在
//    @Data
//    static class Exists{
//
//    }
    // 文件不存在或者部分存在
    @Data
    @AllArgsConstructor
    public static class PartInfo {
        List<Integer> needUploadPartNum;
        String uploadId;
    }
}
