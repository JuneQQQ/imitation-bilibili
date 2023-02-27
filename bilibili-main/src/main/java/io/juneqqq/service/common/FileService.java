package io.juneqqq.service.common;

import io.juneqqq.pojo.dao.entity.FileInfo;
import io.juneqqq.pojo.dto.request.MultipartUploadRequest;
import io.juneqqq.pojo.dto.response.FileUploadResponse;
import io.juneqqq.pojo.dto.response.MultipartUploadCreateResponse;
import io.juneqqq.pojo.vo.ListObjectVo;
import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface FileService {
    FileInfo getFileInfoById(Long id);

    FileUploadResponse upload(File file, String bucket, String hash);

    void download(String bucket, String objectName, File file);

    FileUploadResponse upload(MultipartFile file, String bucket, String hash);

    MultipartUploadCreateResponse createMultipartUpload(MultipartUploadRequest mur);

    FileUploadResponse mergeMultipartUpload(MultipartUploadRequest mur);


    Boolean bucketExists(String bucketName);

    String preview(String fileName, String bucketName);

    StatObjectResponse getFileInfoFromMinio(String bucket, String objectName);

    FileInfo getFileInfoFromDB(String bucket, String hash);

    void remove(String fileName, String bucket);


    List<ListObjectVo> listBucketInfo(String bucketName, String prefix);


    GetObjectResponse getFileInputStream(String fileName, String bucket, String hash, Map<String, String> header);

    GetObjectResponse getFileInputStream(
            String objectName,
            String bucket,
            long offset, long length,
            Map<String, String> header
    );

    GetObjectResponse getFileInputStream(
            String fileName,
            String bucket,
            String hash,
            long offset, long length,
            Map<String, String> header
    );

}
