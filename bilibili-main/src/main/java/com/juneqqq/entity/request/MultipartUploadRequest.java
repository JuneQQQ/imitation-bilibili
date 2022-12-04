package com.juneqqq.entity.request;

import lombok.Data;

/**
 * @author juneqqq
 * @version 1.0
 **/
@Data
// 合并分片请求类
public class MultipartUploadRequest {

    private String fileName;

    private String hash;

    private String finalName;

    public String getFinalName() {
        return this.hash + "-" + this.fileName;
    }

    private String bucket;

    private String uploadId;

    private Integer chunkSize;  // 分片数量

    private Integer duration;  // 视频时长，单位s


    private Long fileSize;

    private String contentType;

    private String pass;  // 密码

    private Integer expire; // 超时时间

    private Integer maxGetCount; // 最大下载数


}