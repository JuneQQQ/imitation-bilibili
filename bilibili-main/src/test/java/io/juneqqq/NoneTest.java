package io.juneqqq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.juneqqq.pojo.dao.entity.UserInfo;
import io.juneqqq.pojo.dao.repository.esmodel.EsUserInfoDto;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoneTest {
    @Test
    void test() {
        System.out.println(StandardCharsets.UTF_8.toString());
    }

    @Test
    void testCopyProperties() {
        UserInfo e1 = new UserInfo();
        e1.setCoin(1);
        e1.setAvatar("1");
        EsUserInfoDto e2 = new EsUserInfoDto();
        e2.setAvatar("bbb");
        e2.setCoin(2222);
        e2.setId(1L);
        e2.setLevel(1);
        BeanUtil.copyProperties(e1,e2,CopyOptions.create().setIgnoreNullValue(true));
//        BeanUtil.copyProperties(e1,e2, true,CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        System.out.println(e2);
    }

    @Test
    void testFastJson() {
        EsUserInfoDto esUserInfoDto = JSONObject.parseObject("""
                {_class=io.juneqqq.dao.repository.esmodel.EsUserInfoDto, id=-6440838546514878865, userId=8355743896386599199, nick=詹瑞, sign=tmemslsuxz5iarry8f9f, avatar=01ko1fyv07p7xmowbtxx, gender=1, birth=2010-11-30, createTime=2022-12-07T17:55:51.209, updateTime=2022-12-07T17:55:51.209}
                """, EsUserInfoDto.class,
                Feature.IgnoreNotMatch);
        System.out.println(esUserInfoDto);
    }
    @Test
    void testOptional() {
        Object abc = Optional.ofNullable(null).orElse("abc");
        System.out.println(abc);
    }

    @Test
    void name() {
        int a = 999999999 / 60 / 60;
        System.out.println(a);
    }

    @Test
    void testHttp() {
        HttpRequest get = HttpUtil.createGet("http://127.0.0.1:15005/minio/file-inputstream?fileName=MySQL%E6%98%AF%E6%80%8E%E6%A0%B7%E8%BF%90%E8%A1%8C%E7%9A%84%EF%BC%9A%E4%BB%8E%E6%A0%B9%E5%84%BF%E4%B8%8A%E7%90%86%E8%A7%A3MySQL.pdf&bucket=default&hash=9ec9faf2ad03699ac9d1523ced366cf2");
        System.out.println(get);
    }

    @Test
    void testBase64() {
        String decode = null;
        decode = URLDecoder.decode("http://124.222.22.217:9000/video/2f957d21533febecdbdd932a1b24c921-%E4%B8%80%E5%91%A8%E5%88%B7%E7%88%86LeetCode%EF%BC%8C%E7%AE%97%E6%B3%95%E5%A4%A7%E7%A5%9E%E5%B7%A6%E7%A5%9E%EF%BC%88%E5%B7%A6%E7%A8%8B%E4%BA%91%EF%BC%89%E8%80%97%E6%97%B6100%E5%A4%A9%E6%89%93%E9%80%A0%E7%AE%97%E6%B3%95%E4%B8%8E%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84%E5%9F%BA%E7%A1%80%E5%88%B0%E9%AB%98%E7%BA%A7%E5%85%A8%E5%AE%B6%E6%A1%B6%E6%95%99%E7%A8%8B%EF%BC%8C%E7%9B%B4%E5%87%BBBTAJ%E7%AD%89%E4%B8%80%E7%BA%BF%E5%A4%A7%E5%8E%82%E5%BF%85%E9%97%AE%E7%AE%97%E6%B3%95%E9%9D%A2%E8%AF%95%E9%A2%98%E7%9C%9F%E9%A2%98%E8%AF%A6%E8%A7%A3%20-%20021%20-%2019.%E4%B8%AD%E7%BA%A7%E6%8F%90%E5%8D%87%E7%8F%AD-2.mp4", StandardCharsets.UTF_8);
        System.out.println(decode);
    }

    @Test
    void testReg() {
        String pattern = "bytes=0-1000";
        String[] split = pattern.split("bytes=|-");
        System.out.println(Arrays.toString(split));
    }

    @Test
    void testMap() {
        HashMap<Integer, Object> map = new HashMap<>();
        map.put(1, "20");
        Long o = (Long) map.get(1);
        System.out.println(o);
    }

    /**
     * 生成100个大小6M的文件  t0~t99
     *
     * @throws IOException
     */
    @Test
    void generateFile() throws IOException {
        File f = new File("/Users/june/Desktop/test/t0");
        FileOutputStream fos = new FileOutputStream(f);
        byte[] bytes = new byte[1024 * 1024 * 6]; // 6M
        for (int i = 0; i < 100; i++) {
            fos.write(bytes);
            fos.close();
            f = new File("/Users/june/Desktop/test/t" + i);
            fos = new FileOutputStream(f);
        }
        fos.write(bytes);
        fos.close();
    }

    @Test
    void test001() throws ExecutionException, InterruptedException {
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(5);
        CompletableFuture<Set<Integer>> c1 = CompletableFuture.supplyAsync(() -> {
            // 抽取roleIds
            return Set.of(1, 23, 6, 4, 32432);
        }, threadPoolExecutor);
        CompletableFuture<Void> c2 = c1.thenAcceptAsync((a) -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(a);
        });
        CompletableFuture<Void> c3 = c1.thenAcceptAsync((a) -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(a);
        });

        CompletableFuture.allOf(c1, c2, c3).get();
    }
}
