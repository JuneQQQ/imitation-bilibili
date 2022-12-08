package io.juneqqq.pojo.vo;

import io.juneqqq.constant.FileStatusEnum;
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

    // 文件不存在或者部分存在
    @Data
    @AllArgsConstructor
    public static class PartInfo {
        List<Integer> needUploadPartNum;
        String uploadId;
    }
}
