package io.juneqqq.pojo.dto.response;

import io.juneqqq.constant.FileStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author juneqqq
 * @version 1.0
 * @date 2022/4/21 9:40
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 分片上传创建响应类
public class MultipartUploadCreateResponse {
    private String uploadId; // 上传编号

    private List<ChunkInfo> chunks; // 分片信息

    private FileStatusEnum fileStatusEnum;  //是否已存在，判断是否需要重传/断点续传/秒传
    private String fileId;  //若已存在，则会返回fileId

    @Data
    public static class ChunkInfo {

        private Integer partNumber; // 分片编号

        private String uploadUrl; // 上传地址
    }

}