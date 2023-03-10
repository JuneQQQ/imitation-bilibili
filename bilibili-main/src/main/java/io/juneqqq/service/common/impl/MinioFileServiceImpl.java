package io.juneqqq.service.common.impl;


import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.juneqqq.config.PearlMinioClient;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.core.exception.ErrorCodeEnum;
import io.juneqqq.pojo.dao.mapper.FileInfoMapper;
import io.juneqqq.pojo.dto.request.MultipartUploadCreate;
import io.juneqqq.constant.FileStatusEnum;
import io.juneqqq.cache.CacheConstant;
import io.juneqqq.pojo.dao.entity.FileInfo;
import io.juneqqq.pojo.dto.request.MultipartUploadRequest;
import io.juneqqq.pojo.dto.response.FileUploadResponse;
import io.juneqqq.pojo.dto.response.MultipartUploadCreateResponse;
import io.juneqqq.pojo.vo.FileStatus;
import io.juneqqq.pojo.vo.ListObjectVo;
import io.juneqqq.service.common.FileService;
import io.juneqqq.util.MinioHelper;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import io.minio.messages.Part;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static io.juneqqq.core.exception.ErrorCodeEnum.MINIO_UNKNOW_EXCEPTION;


@Service("MinioFileServiceImpl")
@Slf4j
public class MinioFileServiceImpl implements FileService {

    @Resource
    private MinioHelper minioHelper;

    @Resource
    private PearlMinioClient client;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private FileInfoMapper fileInfoMapper;

    public FileInfo getFileInfoById(Long id) {
        return fileInfoMapper.selectById(id);
    }


    /**
     * ????????????
     */
    public FileUploadResponse upload(File file, String bucket, String hash) {
        Assert.notNull(file, "??????????????????");
        log.debug("start file upload");

        //????????????
        try {
            return minioHelper.uploadFile(file, bucket, hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ?????????????????????
     */
    public void download(String bucket, String objectName, File file) {
        minioHelper.download(file, objectName, bucket);
    }


    /**
     * ????????????
     */
    public FileUploadResponse upload(MultipartFile file, String bucket, String hash) {
        Assert.notNull(file, "??????????????????");
        log.debug("start file upload");

        //????????????
        try {
            return minioHelper.uploadFile(file, bucket, hash);
        } catch (IOException e) {
            log.error("file upload error.", e);
            throw new BusinessException(ErrorCodeEnum.MINIO_FILE_IO_ERROR);
        } catch (InsufficientDataException e) {
            log.error("insufficient data throw exception", e);
            throw new BusinessException(ErrorCodeEnum.MINIO_INSUFFICIENT_DATA);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.MINIO_UNKNOW_EXCEPTION);
        }
    }


    /**
     * ????????????????????????????????????
     *
     * @return ???
     * code = 2000  ???????????????????????????????????????
     * code = 2001  ????????????????????????????????????/??????
     */
    public MultipartUploadCreateResponse createMultipartUpload(MultipartUploadRequest mur) {
        log.debug("mur:{}", mur);
        // ?????????????????????????????????
        FileStatus fs = checkPreMultipartUpload(mur);

        switch (fs.getStatus()) {
            case FILE_COMPLETELY_EXISTS -> {
                Long fileHash = getFileIdByHash(mur);
                return MultipartUploadCreateResponse.builder()
                        .fileId(String.valueOf(fileHash))
                        .fileStatusEnum(FileStatusEnum.FILE_COMPLETELY_EXISTS)
                        .build();
            }
            case FILE_NEED_MERGE -> {
                mergeMultipartUpload(mur);
                return MultipartUploadCreateResponse.builder()
                        .fileStatusEnum(FileStatusEnum.FILE_COMPLETELY_EXISTS)   // ????????????????????????????????????????????????????????????
                        .build();
            }
            case FILE_PARTLY_EXISTS -> {
                // ???????????????????????????
                List<MultipartUploadCreateResponse.ChunkInfo> chunksInfo
                        = prepareChunksInfo(fs.getPartInfo().getNeedUploadPartNum(), mur);
                return MultipartUploadCreateResponse.builder()
                        .chunks(chunksInfo)
                        .uploadId(fs.getPartInfo().getUploadId())
                        .fileStatusEnum(FileStatusEnum.FILE_PARTLY_EXISTS)
                        .build();
            }
            case FILE_NOT_EXISTS -> {
                // ????????????????????????
                checkBucket(mur.getBucket());

                String uploadId = getUploadId(mur);
                mur.setUploadId(uploadId);
                List<MultipartUploadCreateResponse.ChunkInfo> chunksInfo
                        = prepareChunksInfo(fs.getPartInfo().getNeedUploadPartNum(), mur);
                return MultipartUploadCreateResponse.builder()
                        .chunks(chunksInfo)
                        .uploadId(uploadId)
                        .fileStatusEnum(FileStatusEnum.FILE_NOT_EXISTS)
                        .build();
            }
        }
        throw new BusinessException(ErrorCodeEnum.MINIO_UNKNOW_EXCEPTION);
    }

    @SneakyThrows
    private void checkBucket(String bucket) {
        if (Boolean.FALSE.equals(client.bucketExists(BucketExistsArgs.builder()
                .bucket(bucket)
                .build()).get())) {
            client.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucket)
                    .build());
        }
    }

    @SneakyThrows
    private Long getFileIdByHash(MultipartUploadRequest mur) {
        FileInfo fileInfo = fileInfoMapper.selectOne(new LambdaQueryWrapper<>(FileInfo.class)
                .select(FileInfo::getId).eq(FileInfo::getHash, mur.getHash()));
        if (fileInfo == null) {
            log.warn("Minio???????????????????????????????????????????????????");
            StatObjectResponse response = getFileInfoFromMinio(mur.getBucket(), mur.getFinalName());
            if (!response.object().equals(mur.getFinalName())) {
                remove(response.bucket(), response.object());
                throw new BusinessException(ErrorCodeEnum.MINIO_FILE_NAME_NOT_MATCH_HASH);
            }
            fileInfo = FileInfo.builder()
                    .fileName(mur.getFileName())
                    .hash(mur.getHash())
                    .bucket(response.bucket())
                    .createTime(response.lastModified().toLocalDateTime())
                    .updateTime(response.lastModified().toLocalDateTime())
                    .size(response.size())
                    .etag(response.etag())
                    .uploadId(null)
                    .url(null)  // ???????????????????????????????????????????????????
                    .build();
            fileInfoMapper.insert(fileInfo);
        }
        return fileInfo.getId();
    }

    private String getUploadId(MultipartUploadRequest mur) {
        final MultipartUploadCreate muc = MultipartUploadCreate.builder()
                .bucketName(mur.getBucket())
                .objectName(mur.getFinalName())
                .build();
        final CreateMultipartUploadResponse cmur = minioHelper.uploadId(muc);
        return cmur.result().uploadId();
    }

    private List<MultipartUploadCreateResponse.ChunkInfo> prepareChunksInfo(
            List<Integer> needUploadPartNum,
            MultipartUploadRequest mur
    ) {
        log.debug("????????????????????????, createRequest: [{}]", mur);
        // ????????????
        List<MultipartUploadCreateResponse.ChunkInfo> chunks = new ArrayList<>();
        stringRedisTemplate.opsForValue().set(CacheConstant.FILE_FINAL + mur.getHash(), mur.getUploadId(), Duration.ofDays(1));

        // ??????????????????????????? getPresignedObjectUrl
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("uploadId", mur.getUploadId());
        for (int i = 0; i < mur.getChunkSize(); i++) {
            if (!needUploadPartNum.contains(i + 1)) continue;
            // ?????????????????????????????? partNumber ?????????
            reqParams.put("partNumber", String.valueOf(i + 1));
            // ?????????????????????????????????
            String presignedObjectUrl = minioHelper.getPresignedObjectUrl(
                    mur.getBucket(),
                    mur.getFinalName(),
                    reqParams);
            // ???????????????
            MultipartUploadCreateResponse.ChunkInfo item = new MultipartUploadCreateResponse.ChunkInfo();
            item.setPartNumber(i);
            item.setUploadUrl(presignedObjectUrl);
            chunks.add(item);
        }
        log.debug("??????/????????????????????????, chunks: [{}]", chunks);
        return chunks;
    }

    @Resource
    private ThreadPoolExecutor executor;

    @SneakyThrows
    private FileStatus checkPreMultipartUpload(MultipartUploadRequest mur) {
        FileStatus fs = new FileStatus();

        CompletableFuture<Void> l1 = CompletableFuture.runAsync(() -> {
            String uploadId = stringRedisTemplate.opsForValue().get(CacheConstant.FILE_FINAL + mur.getHash());
            ListPartsResponse lpr = null;
            List<Integer> list = new ArrayList<>();
            for (int i = 1; i <= mur.getChunkSize(); i++) list.add(i);
            if (uploadId != null) {
                try {
                    lpr = minioHelper.listMultipart(MultipartUploadCreate.builder()
                            .bucketName(mur.getBucket())
                            .uploadId(uploadId)
                            .objectName(mur.getFinalName())
                            .build());
                } catch (Exception e) {
                    log.debug("??????????????????uploadId?????????minio??????");
                }
                if (lpr != null) {
                    // uploadId ??????
                    mur.setUploadId(uploadId);
                    // ?????????????????????????????????????????????????????????????????????minio??????
                    if (lpr.result().partList().size() == mur.getChunkSize()) {
                        // ???????????????????????????
                        fs.setStatus(FileStatusEnum.FILE_NEED_MERGE);
                        fs.setPartInfo(new FileStatus.PartInfo(new ArrayList<>(), uploadId));
                    } else {
                        // ?????????????????????????????????
                        fs.setStatus(FileStatusEnum.FILE_PARTLY_EXISTS);
                        for (Part part : lpr.result().partList()) {
                            // ????????????????????????
                            list.remove(Integer.valueOf(part.partNumber()));
                        }
                        fs.setPartInfo(new FileStatus.PartInfo(list, uploadId));
                    }
                } else {
                    // redis ??? uploadId ?????????????????????????????????  ???????????????uploadId
                    fs.setStatus(FileStatusEnum.FILE_NOT_EXISTS);
                    fs.setPartInfo(new FileStatus.PartInfo(list, null));
                }
            } else {
                // ?????? uploadId ??????????????????
                fs.setStatus(FileStatusEnum.FILE_NOT_EXISTS);
                fs.setPartInfo(new FileStatus.PartInfo(list, null));
            }
        }, executor);

        Result<Item> item = CompletableFuture.supplyAsync(() -> {
            Iterator<Result<Item>> iterator = client.listObjects(
                    ListObjectsArgs.builder().
                            bucket(mur.getBucket()).
                            prefix(mur.getHash()).build()).iterator();
            Result<Item> next;
            if (iterator.hasNext()) {
                next = iterator.next();
                try {
                    next.get();
                } catch (Exception e) {
                    fs.setStatus(FileStatusEnum.FILE_NOT_EXISTS);
                    return next;
                }
                if (iterator.hasNext()) {
                    log.error("?????????????????????hash?????????????????????????????????bucket:{};hash:{}",
                            mur.getBucket(), mur.getHash());
                    throw new BusinessException(MINIO_UNKNOW_EXCEPTION);
                }
                fs.setStatus(FileStatusEnum.FILE_COMPLETELY_EXISTS);
                return next;
            } else {
                return null;
            }
        }, executor).get();

        if (item == null) {
            l1.get();
        }
        return fs;
    }

    /**
     * ????????????
     */
    public FileUploadResponse mergeMultipartUpload(MultipartUploadRequest mur) {
        log.debug("??????????????????, mur: [{}]", mur);

        // ??????fileName

        log.debug("final name???" + mur.getFinalName());

        try {
            final ListPartsResponse listMultipart = minioHelper.listMultipart(MultipartUploadCreate
                    .builder()
                    .bucketName(mur.getBucket())
                    .objectName(mur.getFinalName())
                    .maxParts(mur.getChunkSize() + 1)
                    .uploadId(mur.getUploadId())
                    .partNumberMarker(0)
                    .build()
            );
            // ????????????????????????
            final ObjectWriteResponse owr = minioHelper.completeMultipartUpload(MultipartUploadCreate.builder()
                    .bucketName(mur.getBucket())
                    .uploadId(mur.getUploadId())
                    .objectName(mur.getFinalName())
                    .parts(listMultipart.result().partList().toArray(new Part[]{}))
                    .build());
            log.debug(String.valueOf(owr));

            FileInfo fileInfo = new FileInfo(
                    null,
                    URLDecoder.decode(owr.region(), StandardCharsets.UTF_8),
                    mur.getBucket(),
                    mur.getFileName(),
                    mur.getHash(),    // hash
                    mur.getUploadId(),   // uploadId
                    owr.etag().substring(1, owr.etag().length() - 1),  // etag-> "" ???????????????????????????
                    mur.getFileSize(),
                    null, null
            );
            fileInfoMapper.insert(fileInfo);

            log.debug("fileId is:" + fileInfo.getId());

            return FileUploadResponse.builder()
                    .url(minioHelper.minioProperties.getEndpoint() + "/" +
                            mur.getBucket() + "/" + mur.getFinalName())
                    .fileId(String.valueOf(fileInfo.getId()))   // ??????Number?????????16??? ???long????????????
                    .build();
        } catch (Exception e) {
            log.error("??????????????????", e);
        }
        log.debug("??????????????????, mur: [{}]", mur);
        return null;
    }

    public void remove(String fileName, String bucket) {
        if (StringUtils.isBlank(fileName)) return;
        log.debug("??????????????????, fileName: [{}]", fileName);
        try {
            minioHelper.removeFile(fileName, bucket);
        } catch (Exception e) {
            log.error("??????????????????", e);
        }
        log.debug("??????????????????, fileName: [{}]", fileName);
    }

    /**
     * ????????????bucket????????????
     *
     * @return boolean
     */
    public Boolean bucketExists(String bucketName) {
        boolean found;
        try {
            found = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return found;
    }


    /**
     * ????????????
     */
    public String preview(String fileName, String bucketName) {

        GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs.builder().
                bucket(bucketName).
                object(fileName).
                method(Method.GET).
                build();
        try {
            return client.getPresignedObjectUrl(build);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * ???minio???????????????
     *
     * @param objectName ????????????????????????
     */
    @SneakyThrows
    public StatObjectResponse getFileInfoFromMinio(String bucket, String objectName) {
        return client.statObject(StatObjectArgs.builder()
                .bucket(bucket).
                object(objectName).build()).get();
    }

    /**
     * ??????
     */
    public FileInfo getFileInfoFromDB(String bucket, String hash) {
        return fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfo>().
                eq(FileInfo::getBucket, bucket).
                eq(FileInfo::getHash, hash));
    }


    /**
     * ????????????????????????
     *
     * @return ??????bucket?????????????????????
     */
    public List<ListObjectVo> listBucketInfo(String bucketName, String prefix) {
        ListObjectsArgs.Builder builder = ListObjectsArgs.builder();
        if (bucketName != null) builder.bucket(bucketName);
        if (prefix != null) builder.prefix(prefix);
        Iterable<Result<Item>> results = client.listObjects(builder.build());
        List<ListObjectVo> vos = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                log.debug("result???" + result.get().objectName());
                log.debug("result???" + result.get().storageClass());

                ListObjectVo l = new ListObjectVo(
                        result.get().objectName(),
                        result.get().etag()
                );
                vos.add(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return vos;
    }


    /**
     * ????????????
     */
    public GetObjectResponse getFileInputStream(String fileName, String bucket, String hash, Map<String, String> header) {
        GetObjectArgs objectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .extraHeaders(header)
                .object(hash + "-" + fileName).build();
        try {
            return client.getObject(objectArgs).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public GetObjectResponse getFileInputStream(
            String objectName,
            String bucket,
            long offset, long length,
            Map<String, String> header
    ) {
        GetObjectArgs objectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .offset(offset)
                .length(length)
                .extraHeaders(header)
                .build();
        try {
            return client.getObject(objectArgs).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GetObjectResponse getFileInputStream(
            String fileName,
            String bucket,
            String hash,
            long offset, long length,
            Map<String, String> header
    ) {
        GetObjectArgs objectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(hash + "-" + fileName)
                .offset(offset)
                .length(length)
                .extraHeaders(header)
                .build();
        try {
            return client.getObject(objectArgs).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
