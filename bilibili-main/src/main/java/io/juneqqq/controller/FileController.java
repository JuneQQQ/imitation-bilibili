package io.juneqqq.controller;

import io.juneqqq.constant.CacheConstant;
import io.juneqqq.core.auth.auth.ApiRouterConstant;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.core.exception.ErrorCodeEnum;
import io.juneqqq.dao.entity.FileInfo;
import io.juneqqq.dao.entity.R;
import io.juneqqq.pojo.dto.request.MultipartUploadRequest;
import io.juneqqq.pojo.dto.response.FileUploadResponse;
import io.juneqqq.pojo.dto.response.MultipartUploadCreateResponse;
import io.juneqqq.pojo.vo.ListObjectVo;
import io.juneqqq.service.common.FileService;
import io.minio.GetObjectResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@Tag(name = "FileController",description = "文件上传模块")
@RequestMapping(ApiRouterConstant.API_FRONT_RESOURCE_URL_PREFIX)
public class FileController {
    @Resource(name = "MinioFileServiceImpl")
    private FileService fileService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/files-info")
    public R<FileInfo> getFileInfo(String bucket, String hash) {
        FileInfo fi = fileService.getFileInfoFromDB(bucket, hash);
        return R.ok(fi);
    }

    @GetMapping("/bucket-files")
    public R<List<ListObjectVo>> getBucketInfo(String bucket, String prefix) {
        List<ListObjectVo> items = fileService.listBucketInfo(bucket, prefix);
        return R.ok(items);
    }

    @GetMapping("/preview-files")
    public R<String> getPreviewURL(String bucket, String fileName) {
        String url = fileService.preview(fileName, bucket);
        return R.ok(url);
    }


    /**
     * 获取全部流
     */
    @GetMapping("/file-inputstream")
    public R<Void> getObject(String fileName, String bucket, String hash, HttpServletResponse response) throws IOException {
        GetObjectResponse is = fileService.getFileInputStream(fileName, bucket, hash, null);
        response.setCharacterEncoding("utf-8");
//        // 设置强制下载不打开
        // res.setContentType("application/force-download");
        String size = stringRedisTemplate.opsForValue().get(CacheConstant.FILE_SIZE_CACHE_NAME + hash);
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
            stringRedisTemplate.opsForValue().set(CacheConstant.FILE_SIZE_CACHE_NAME + hash, String.valueOf(total));
        is.close();
        return R.ok();
    }

    /**
     * 获取一部分流
     */
    @GetMapping("/file-offset-inputstream")
    public R<Void> getObject(String fileName, String bucket, String hash, long offset, long length, HttpServletResponse response) throws IOException {
        InputStream is = fileService.getFileInputStream(fileName, bucket, hash, offset, length, null);
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Length", String.valueOf(length));
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        byte[] buf = new byte[1024 * 16]; // 16kb
        int len;
        long skip = is.skip(offset);
        if (skip < offset) throw new BusinessException(ErrorCodeEnum.FILE_OFFSET_TOO_LARGE);
        try (ServletOutputStream os = response.getOutputStream()) {
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, (int) Math.min(len, length));
                length -= len;
            }
        }
        is.close();
        return R.ok();
    }

    /**
     * 普通上传
     */
    @PostMapping("/files")
    public R<Void> uploadMinioFile(
            MultipartFile file,
            String bucket,
            String hash
    ) {
        log.debug("file：" + file.getOriginalFilename());
        FileUploadResponse upload = fileService.upload(file, bucket, hash);

        return R.ok();
    }

    // 创建分片上传
    @PostMapping("/multipart/create")
    public R<MultipartUploadCreateResponse> createMultipartUpload(
            @RequestBody
            MultipartUploadRequest mur
    ) {
        MultipartUploadCreateResponse multipartUpload = fileService.createMultipartUpload(mur);
        return R.ok(multipartUpload);
    }

    // 合并分片
    @CrossOrigin
    @PostMapping("/multipart/merge")
    public R<FileUploadResponse> completeMultipartUpload(
            @RequestBody
            MultipartUploadRequest uploadRequest
    ) {
        FileUploadResponse fur = fileService.mergeMultipartUpload(uploadRequest);
        return R.ok(fur);
    }


}