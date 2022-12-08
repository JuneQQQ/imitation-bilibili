package io.juneqqq;

import io.juneqqq.config.PearlMinioClient;
import io.juneqqq.pojo.dto.request.MultipartUploadCreate;
import io.juneqqq.service.common.FileService;
import io.juneqqq.util.MinioHelper;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MinioTest {

    @Test
    void downloadLocal() throws IOException {
//        File f = File.createTempFile("mytmp",".mp4");
        File f = new File("/Users/june/Desktop/tmp.mp4");
        minioHelper.download(f,"1313decadba96c01b805781bfcd39168","video");
        System.out.println(f.getAbsolutePath());
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Resource
    private PearlMinioClient client;

    @Resource
    public MinioHelper minioHelper;

    @Resource(name = "MinioFileServiceImpl")
    public FileService fileService;


    @Test
    void testGetUploadId() throws InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, ExecutionException, XmlParserException, InterruptedException, InternalException {
        // 这个ID已完全上传
        // NGY0ZDZkNmUtMjBhNy00NzYzLThjODQtOWM4OTQ2NWNmYWE1LjZlM2ZiOGEwLWQ0NTMtNDhjZS1iZWRmLTQ4ZDlkZjBiZjRkNQ%3D%3D
        ListPartsResponse res = minioHelper.listMultipart(MultipartUploadCreate.builder()
                .bucketName("default")
                .uploadId("NGY0ZDZkNmUtMjBhNy00NzYzLThjODQtOWM4OTQ2NWNmYWE1LjU1OWVkZDFmLTEzNTEtNDc3NC05Y2M4LTA0Y2Q5NjM3MWZiMg==")
                .objectName("de93a2e768e3db477fa4db491c075b78-笑傲java面试.xmind").partNumberMarker(0).maxParts(13)
                .build());

        log.debug("success！part size:" + res.result().partList());
        for (Part part : res.result().partList()) {
            System.out.println("etag" + part.etag());
            System.out.println("no" + part.partNumber());
            System.out.println("size" + part.partSize());
        }

        ObjectWriteResponse aDefault = minioHelper.completeMultipartUpload(MultipartUploadCreate.builder()
                .bucketName("default")
                .uploadId("NGY0ZDZkNmUtMjBhNy00NzYzLThjODQtOWM4OTQ2NWNmYWE1LjU1OWVkZDFmLTEzNTEtNDc3NC05Y2M4LTA0Y2Q5NjM3MWZiMg==")
                .objectName("de93a2e768e3db477fa4db491c075b78-笑傲java面试.xmind")
                .parts(res.result().partList().toArray(new Part[]{}))
                .build());
//        ListPartsResponse aDefault = client.listMultipart("default", null, "笑傲java面试.xmind", 13, 0,
//                "NGY0ZDZkNmUtMjBhNy00NzYzLThjODQtOWM4OTQ2NWNmYWE1LmE0ZjdkOWUxLWZmODAtNDczZi05M2NkLTY2YTBlMThmOWZlYQ==", null, null);
//        System.out.println(aDefault.result().partList());
    }

    //
    @Test
    void testUpload() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, ExecutionException, InterruptedException {
        File f = new File("/Users/june/Desktop/test/t0");
        InputStream is = new FileInputStream(f);
        long l = System.currentTimeMillis();
        for (int i = 1; i < 100; i++) {
            client.putObject(PutObjectArgs.builder()
                    .bucket("default")
                    .object("test" + (i - 1))
                    .stream(is, is.available(), -1)
                    .build()).get();
            f = new File("/Users/june/Desktop/test/t" + i);
            is = new FileInputStream(f);
        }
        log.debug("预热完成");
        // 16M ->
        // 500M ->
        // 1G ->
        // 5G ->
        f = new File("/Users/june/Desktop/test16.txt");
        is = new FileInputStream(f);
        client.putObject(PutObjectArgs.builder()
                .object("test16")
                .bucket("default")
                .stream(is, is.available(), -1)
                .build()).get();
        long l1 = System.currentTimeMillis();
        log.debug("16M:" + (l1 - l) / 1000);
        f = new File("/Users/june/Desktop/test200.txt");
        is = new FileInputStream(f);
        client.putObject(PutObjectArgs.builder()
                .bucket("default")
                .object("test200")
                .stream(is, is.available(), -1)
                .build()).get();

        long l2 = System.currentTimeMillis();
        log.debug("200M:" + (l2 - l1) / 1000);
        f = new File("/Users/june/Desktop/test1000.txt");
        is = new FileInputStream(f);
        client.putObject(PutObjectArgs.builder()
                .bucket("default")
                .object("test1000")
                .stream(is, is.available(), -1)
                .build()).get();
        long l3 = System.currentTimeMillis();
        log.debug("1G:" + (l3 - l2) / 1000);
    }

}
