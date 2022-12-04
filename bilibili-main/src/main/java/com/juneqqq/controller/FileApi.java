package com.juneqqq.controller;

import com.juneqqq.entity.constant.RedisPrefix;
import com.juneqqq.entity.dao.FileInfo;
import com.juneqqq.entity.dao.R;
import com.juneqqq.entity.exception.CustomException;
import com.juneqqq.entity.request.MultipartUploadRequest;
import com.juneqqq.entity.response.FileUploadResponse;
import com.juneqqq.entity.response.MultipartUploadCreateResponse;
import com.juneqqq.entity.vo.ListObjectVo;
import com.juneqqq.service.common.FileService;
import io.minio.GetObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
public class FileApi {
    @Resource
    private FileService fileService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/files-info")
    public R<FileInfo> getFileInfo(String bucket,String hash) {
        FileInfo fi = fileService.getFileInfoFromMinioDB(bucket,hash);
        return new R<>(fi);
    }

//    @GetMapping("/minio/file-info")
//    public R<GetObjectResponse> getFileInfo(String bucket,String objectName) {
//        GetObjectResponse gor = fileService.getFileInfoFromMinio(bucket, objectName);
//        return new R<>(gor);
//    }


    @GetMapping("/bucket-files")
    public R<List<ListObjectVo>> getBucketInfo(String bucket, String prefix) {
        List<ListObjectVo> items = fileService.listBucketInfo(bucket, prefix);
        return new R<>(items);
    }

    @GetMapping("/preview-files")
    public R<String> getPreviewURL(String bucket, String fileName) {
        String url = fileService.preview(fileName, bucket);
        return new R<>(url);
    }


    /**
     * 获取全部流
     */
    @GetMapping("/file-inputstream")
    public R<String> getObject(String fileName, String bucket, String hash, HttpServletResponse response) throws IOException {
        GetObjectResponse is = fileService.getFileInputStream(fileName, bucket, hash,null);
        response.setCharacterEncoding("utf-8");
//        // 设置强制下载不打开
        // res.setContentType("application/force-download");
        String size = stringRedisTemplate.opsForValue().get(RedisPrefix.FILE_SIZE + hash);
        if (size != null) response.setHeader("Content-Length", size);
        fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        byte[] buf = new byte[1024 * 16]; // 16kb
        int len;
        int total = 0;
        try (ServletOutputStream os = response.getOutputStream()) {
            while ((len = is.read(buf)) != -1) {
                total += len;
                os.write(buf, 0, len);
            }
        }
        // >=10M才有保存的必要
        if (size == null && total >= 1024 * 1024 * 10)
            stringRedisTemplate.opsForValue().set(RedisPrefix.FILE_SIZE + hash, String.valueOf(total));
        is.close();
        return R.success();
    }

    /**
     * 获取一部分流
     */
    @GetMapping("/file-offset-inputstream")
    public R<String> getObject(String fileName, String bucket, String hash, long offset, long length, HttpServletResponse response) throws IOException {
        InputStream is = fileService.getFileInputStream(fileName, bucket, hash, offset, length,null);
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Length", String.valueOf(length));
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        byte[] buf = new byte[1024 * 16]; // 16kb
        int len;
        long skip = is.skip(offset);
        if (skip < offset) throw new CustomException("offset大于fileSize");
        try (ServletOutputStream os = response.getOutputStream()) {
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, (int) Math.min(len, length));
                length -= len;
            }
        }
        is.close();
        return R.success();
    }

    /**
     * 普通上传
     */
    @PostMapping("/files")
    public R<String> uploadMinioFile(
            MultipartFile file,
            String bucket,
            String hash
    ) {
        log.debug("file：" + file.getOriginalFilename());
        FileUploadResponse upload = fileService.upload(file, bucket, hash);

        return R.success();
    }

    // 创建分片上传
    @PostMapping("/multipart/create")
    public R<MultipartUploadCreateResponse> createMultipartUpload(
            @RequestBody
            MultipartUploadRequest mur
    ) {
        MultipartUploadCreateResponse multipartUpload =
                fileService.createMultipartUpload(mur);
        switch (multipartUpload.getFileStatusEnum()) {
            case FILE_COMPLETELY_EXISTS -> {
                return new R<>(2000, multipartUpload);
            }
            case FILE_NEED_MERGE -> {
                return new R<>(2001, multipartUpload);
            }
            case FILE_PARTLY_EXISTS -> {
                return new R<>(2002, multipartUpload);
            }
            case FILE_NOT_EXISTS -> {
                return new R<>(2003, multipartUpload);
            }
        }
        return new R<>(500, "文件状态码一个都不匹配吗？");
    }

    // 合并分片
    @CrossOrigin
    @PostMapping("/multipart/merge")
    public R<FileUploadResponse> completeMultipartUpload(
            @RequestBody
            MultipartUploadRequest uploadRequest
    ) {
        return new R<>(fileService.mergeMultipartUpload(uploadRequest));
    }


}
