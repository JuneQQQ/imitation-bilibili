package io.juneqqq.service.common.impl;


import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.juneqqq.constant.elastic.EsConstant;
import io.juneqqq.constant.elastic.UserInfoIndex;
import io.juneqqq.constant.elastic.VideoIndex;
import io.juneqqq.pojo.dto.PageResult;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.core.exception.ErrorCodeEnum;

import io.juneqqq.pojo.dao.repository.UserInfoDtoRepository;
import io.juneqqq.pojo.dao.repository.VideoDtoRepository;
import io.juneqqq.pojo.dao.repository.esmodel.EsUserInfoDto;
import io.juneqqq.pojo.dao.repository.esmodel.EsVideoDto;
import io.juneqqq.pojo.dto.request.elasticsearch.UserSearchCondition;
import io.juneqqq.pojo.dto.request.elasticsearch.VideoSearchCondition;
import io.juneqqq.pojo.dto.response.elasticsearch.UserSearchResult;
import io.juneqqq.pojo.dto.response.elasticsearch.VideoSearchResult;
import io.juneqqq.service.common.SearchService;
import io.juneqqq.service.common.UserService;
import io.juneqqq.service.common.VideoService;
import io.juneqqq.util.Try;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class ElasticSearchServiceImpl implements SearchService {

    @Resource
    private VideoDtoRepository videoDtoRepository;

    @Resource
    private UserInfoDtoRepository userInfoDtoRepository;

    @Resource
    ElasticsearchClient elasticsearchClient;
    @Resource
    private VideoService videoService;

    @Resource
    private UserService userService;

    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 使配置注解生效，不能配置在配置文件处
     */
    @PostConstruct
    void initIndex() {
        Try.ofFailable(() -> elasticsearchTemplate.indexOps(EsVideoDto.class).putMapping())
                .onFailure(e ->
                        log.warn("{} mapping 添加失败。若有必要，请在kibana使用\n DELETE {}", VideoIndex.NAME, VideoIndex.NAME));

        Try.ofFailable(() -> elasticsearchTemplate.indexOps(EsUserInfoDto.class).putMapping())
                .onFailure(e ->
                        log.warn("{} mapping 添加失败。若有必要，请在kibana使用\n DELETE {}", UserInfoIndex.NAME, UserInfoIndex.NAME));
    }

    /**
     * 搜索用户info
     */
    public PageResult<UserSearchResult> searchUserInfos(
            UserSearchCondition condition
    ) {
        try {
            SearchResponse<EsUserInfoDto> response = elasticsearchClient.search(s -> {
                // 构建检索条件
                buildSearchCondition(condition, s);
                return s;
            }, EsUserInfoDto.class);

            log.debug("es response:" + response.toString());
            return buildSearchResult(response, condition);
        } catch (IOException e) {
            log.error("检索异常，异常信息：{}", e.getMessage());
            throw new BusinessException(ErrorCodeEnum.SEARCH_ERROR);
        }
    }

    private void buildSearchCondition(UserSearchCondition condition, SearchRequest.Builder searchBuilder) {
        // ==================> 索引
        searchBuilder.index(UserInfoIndex.NAME);

        // ==================> 必要条件
        BoolQuery boolQuery = BoolQuery.of(b -> {
            if (StringUtils.isNotBlank(condition.getKeyword())) {
                // 关键词匹配
                b.must((q -> q
                        // 以下几个条件至少匹配一个
                        .multiMatch(t -> t
                                .fields(
                                        UserInfoIndex.FIELD_NICK + "^2",
                                        UserInfoIndex.FIELD_SIGN + "^0.2"
                                )
                                .query(condition.getKeyword())
                        )
                ));
            }
            // 精确查询：分区匹配
            Optional.ofNullable(condition.getGender()).ifPresent(g ->
                    b.must(TermQuery.of(m -> m
                            .field(VideoIndex.FIELD_PARTITION)
                            .value(g)
                    )._toQuery()));
            // 范围条件 =========>
            // 粉丝数
            b.must(RangeQuery.of(r -> r
                            .field(UserInfoIndex.FIELD_FAN_COUNT)
                            .lte(JsonData.of(condition.getMaxFanCount()))
                            .gte(JsonData.of(condition.getMinFanCount())))
                    ._toQuery());
            // 等级
            b.must(RangeQuery.of(r -> r
                            .field(UserInfoIndex.FIELD_FAN_COUNT)
                            .lte(JsonData.of(condition.getMaxLevel()))
                            .gte(JsonData.of(condition.getMinLevel())))
                    ._toQuery());
            return b;
        });
        searchBuilder.query(q -> q.bool(boolQuery));
        // ==================> 排序
        buildSortCondition(condition.getSort(), condition.getIsAsc(), searchBuilder);
        // ==================> 分页
        searchBuilder.from(condition.getCurrent() * condition.getSize())
                .size(condition.getSize());
        // ==================> 设置高亮显示
        searchBuilder.highlight(h -> h
                .fields(UserInfoIndex.FIELD_NICK,
                        t -> t.preTags(EsConstant.STRONG_PRE_TAG).postTags(EsConstant.STRONG_POST_TAG)));
    }

    private static void buildSortCondition(String[] field, Boolean[] isAsc, SearchRequest.Builder searchBuilder) {
        if (field == null || isAsc == null) return;
        assert field.length == isAsc.length;
        // 排序
        for (int i = 0; i < field.length; i++) {
            int finalI = i;
            searchBuilder.sort(o -> o.field(f -> f
                    .field(StringUtils.underlineToCamel(field[finalI]))
                    .order(isAsc[finalI] ? SortOrder.Asc : SortOrder.Desc))
            );
        }
    }

    private PageResult<UserSearchResult> buildSearchResult(SearchResponse<EsUserInfoDto> response, UserSearchCondition condition) {
        TotalHits total = response.hits().total();
        assert total != null;
        List<UserSearchResult> lu = new ArrayList<>();
        for (Hit<EsUserInfoDto> next : response.hits().hits()) {
            EsUserInfoDto source = next.source();
            UserSearchResult usr = new UserSearchResult();
            BeanUtil.copyProperties(source, usr);

            Optional.ofNullable(next.highlight().get(UserInfoIndex.FIELD_NICK))
                    .ifPresent(c -> usr.setNick(c.get(0)));
            lu.add(usr);
        }
        return PageResult.of(condition.getCurrent(), Math.min(condition.getSize(), lu.size()), total.value(), lu);
    }

    public PageResult<VideoSearchResult> searchVideos(
            VideoSearchCondition condition
    ) {
        try {
            SearchResponse<EsVideoDto> response = elasticsearchClient.search(s -> {
                // 构建检索条件
                buildSearchCondition(condition, s);
                return s;
            }, EsVideoDto.class);

            log.debug("es response:" + response.toString());
            return buildSearchResult(response, condition);
        } catch (IOException e) {
            log.error("es搜索异常，异常信息：{}", e.getMessage());
            throw new BusinessException(ErrorCodeEnum.SEARCH_ERROR);
        }
    }

    private PageResult<VideoSearchResult> buildSearchResult(
            SearchResponse<EsVideoDto> response,
            VideoSearchCondition condition
    ) {
        TotalHits total = response.hits().total();
        assert total != null;
        List<VideoSearchResult> lv = new ArrayList<>();
        for (Hit<EsVideoDto> next : response.hits().hits()) {
            EsVideoDto source = next.source();
            VideoSearchResult vsr = new VideoSearchResult();
            BeanUtil.copyProperties(source, vsr);

            Optional.ofNullable(next.highlight().get(VideoIndex.FIELD_USER_NICK))
                    .ifPresent(c -> vsr.setNick(c.get(0)));
            Optional.ofNullable(next.highlight().get(VideoIndex.FIELD_TITLE))
                    .ifPresent(c -> vsr.setTitle(c.get(0)));
            lv.add(vsr);
        }
        return PageResult.of(condition.getCurrent(), Math.min(condition.getSize(), lv.size()), total.value(), lv);
    }

    private void buildSearchCondition(VideoSearchCondition condition, SearchRequest.Builder searchBuilder) {
        // ==================> 索引
        searchBuilder.index(VideoIndex.NAME);

        // ==================> 必要条件
        BoolQuery boolQuery = BoolQuery.of(b -> {
            if (StringUtils.isNotBlank(condition.getKeyword())) {
                // 关键词匹配
                b.must((q -> q
                        // 以下几个条件至少匹配一个
                        .multiMatch(t -> t
                                .fields(VideoIndex.FIELD_TITLE + "^2",
                                        VideoIndex.FIELD_USER_NICK + "^1.5",
                                        VideoIndex.FIELD_DESCRIPTION + "^0.5"
                                )
                                .query(condition.getKeyword())
                        )
                ));
            }
            // 精确查询：分区匹配
            if (Objects.nonNull(condition.getPartition())) {
                b.must(TermQuery.of(m -> m
                        .field(VideoIndex.FIELD_PARTITION)
                        .value(condition.getPartition())
                )._toQuery());
            }
            // 范围条件 =========>
            // 时长
            b.must(RangeQuery.of(r -> r
                    .field(VideoIndex.FIELD_DURATION)
                    .gte(JsonData.of(condition.getMinDuration()))
                    .lte(JsonData.of(condition.getMaxDuration())))._toQuery());

            return b;
        });
        searchBuilder.query(q -> q.bool(boolQuery));

        // 排序
        buildSortCondition(condition.getSort(), condition.getIsAsc(), searchBuilder);

        // ==================> 分页
        searchBuilder.from(condition.getCurrent() * condition.getSize())
                .size(condition.getSize());
        // ==================> 设置高亮显示
        searchBuilder.highlight(h -> h
                .fields(VideoIndex.FIELD_TITLE,
                        t -> t.preTags(EsConstant.STRONG_PRE_TAG).postTags(EsConstant.STRONG_POST_TAG))
                .fields(VideoIndex.FIELD_USER_NICK,
                        t -> t.preTags(EsConstant.STRONG_PRE_TAG).postTags(EsConstant.STRONG_POST_TAG)));
    }
}
