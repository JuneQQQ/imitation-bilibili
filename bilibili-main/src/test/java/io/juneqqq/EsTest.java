package io.juneqqq;

import cn.hutool.core.date.DateField;
import cn.hutool.core.util.RandomUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.xxl.job.core.util.DateUtil;
import io.juneqqq.constant.elastic.UserInfoIndex;
import io.juneqqq.constant.elastic.VideoIndex;
import io.juneqqq.pojo.dao.repository.UserInfoDtoRepository;
import io.juneqqq.pojo.dao.repository.VideoDtoRepository;
import io.juneqqq.pojo.dao.repository.esmodel.EsUserInfoDto;
import io.juneqqq.pojo.dao.repository.esmodel.EsVideoDto;
import io.juneqqq.service.common.SearchService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ListIterator;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EsTest {
    @Resource
    private ElasticsearchClient elasticsearchClient;
    //
    @Resource
    private UserInfoDtoRepository userInfoDtoRepository;
    //
    @Resource
    private VideoDtoRepository videoDtoRepository;


    @Test
    void prepareData() {
        for (int i = 0; i < 100; i++) {
            EsUserInfoDto b = EsUserInfoDto.builder().
                    id(RandomUtil.randomLong(0, 1111111111))
                    .avatar(RandomUtil.randomString(20))
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .sign(RandomUtil.randomString(20))
                    .userId(RandomUtil.randomLong(0, 1111111111))
                    .birth(RandomUtil.randomDate(DateUtil.parseDate("2010-07-20"),
                            DateField.AM_PM, 0, 10000).toLocalDateTime().toLocalDate())
                    .gender(i % 2)
                    .nick("中国")
                    .level(RandomUtil.randomInt(0, 6))
                    .fanCount(RandomUtil.randomInt(888, 100000))
                    .isVip(RandomUtil.randomBoolean())
                    .build();
            userInfoDtoRepository.save(b);
        }

        for (int i = 0; i < 100; i++) {
            EsVideoDto b = EsVideoDto.builder().
                    id(RandomUtil.randomLong(0, 1222222222))
                    .cover(RandomUtil.randomString(20))
                    .fileId(RandomUtil.randomLong(0, 1222222222))
                    .duration(RandomUtil.randomInt(0, 1111111111))
                    .title(RandomValueUtil.getRoad())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .userId(RandomUtil.randomLong(0, 1222222222))
                    .nick("中国")
                    .like(RandomUtil.randomInt(0, 1111111111))
                    .coin(RandomUtil.randomInt(0, 1111111111))
                    .collection(RandomUtil.randomInt(0, 1111111111))
                    .type(0)
                    .description(RandomValueUtil.getRoad())
                    .partition(0)
                    .build();
            videoDtoRepository.save(b);
        }
    }

    @Test
    void testMSearch() throws IOException {
        System.out.println(111);
        SearchResponse<Object> response = elasticsearchClient.search(s -> {
            // 构建检索条件
            s.index(List.of("video", "user-info"));
            return s;
        }, Object.class);
        List<Hit<Object>> hits = response.hits().hits();
        ListIterator<Hit<Object>> hitListIterator = hits.listIterator();
        while (hitListIterator.hasNext()) {
            Hit<Object> next = hitListIterator.next();
            switch (next.index()) {
                case UserInfoIndex.NAME -> {
                    String s = JSONObject.toJSONString(next.source());
                    EsUserInfoDto esUserInfoDto = JSONObject.parseObject(s, EsUserInfoDto.class);
                    System.out.println(s);
//                    EsUserInfoDto esUserInfoDto = JSONObject.parseObject(.toString(), EsUserInfoDto.class, JSONReader.Feature.IgnoreNoneSerializable);
                }
                case VideoIndex.NAME -> {
                    EsVideoDto esUserInfoDto = JSONObject.parseObject(next.source().toString(), EsVideoDto.class, JSONReader.Feature.IgnoreNoneSerializable);
                }
            }
            Object source = next.fields();

            System.out.println(111);
        }

        System.out.println(response);
    }


    @Test
    void load() {
        EsUserInfoDto esUserInfoDto = userInfoDtoRepository.searchByUserId(410499013L);
        System.out.println(esUserInfoDto);
    }


    @Test
    void testBatch(){
        UpdateByQueryRequest.Builder builder = new UpdateByQueryRequest.Builder();

//        BulkRequest.Builder builder = new BulkRequest.Builder();
//        builder.operations(BulkOperation.of(a->{
//            a.update(UpdateOperation.of(b->{
//                b.
//            }))
//        }))
//        elasticsearchClient.bulk();
    }

    @Resource
    private SearchService elasticSearchService;

}
