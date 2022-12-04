package com.juneqqq.entity.response;


import lombok.Builder;
import lombok.Data;

/**
 * @author juneqqq
 * @version 1.0
 * @date 2022/4/12 10:49
 * @description minio文件上传返回类
 **/
@Data
@Builder
public class FileUploadResponse {

    private String fileId;

    private String realName;

    private String uploadName;

    private String url;

    private long size;

    private String bucket;


}