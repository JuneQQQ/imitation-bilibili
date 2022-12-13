package io.juneqqq.core.handler.xxljob;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.jsonwebtoken.lang.Collections;
import io.juneqqq.constant.elastic.UserInfoIndex;
import io.juneqqq.constant.elastic.VideoIndex;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.core.exception.ErrorCodeEnum;
import io.juneqqq.dao.repository.esmodel.EsUserInfoDto;
import io.juneqqq.dao.repository.esmodel.EsVideoDto;
import io.juneqqq.service.common.UserService;
import io.juneqqq.service.common.VideoService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 主要负责es数据的同步任务，一般在凌晨定时执行，全量更新es
 * @author june
 */
@Component
@Slf4j
public class ElasticSearchTask {
    @Resource
    ElasticsearchClient elasticsearchClient;
    @Resource
    private VideoService videoService;
    @Resource
    private UserService userService;

    /**
     * 每月凌晨做一次全量数据同步
     */
    @XxlJob("elasticsearch-update-handler")
    public ReturnT<String> elasticsearchUpdateHandler() {
        try {
//            updateEsVideo();
            updateEsUserInfo();
        } catch (Exception e) {
            log.warn("xxl-job任务出错，错误信息：" + e.getMessage());
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }

    private void updateEsUserInfo() {
        long l1 = System.currentTimeMillis();
        try {
            int size = 1000;
            // 从数据库查size条数据，拼接发往es
            for (int i = 0; ; i++) {
                List<EsUserInfoDto> esUserInfoDtoList = userService.selectBatchEsUserInfoDto(i, size);
                if (Collections.isEmpty(esUserInfoDtoList)) break;
                BulkRequest.Builder br = new BulkRequest.Builder();
                for (EsUserInfoDto euid : esUserInfoDtoList) {
                    br.operations(op -> op
                            .index(idx -> idx
                                    .index(UserInfoIndex.NAME)
                                    .id(String.valueOf(euid.getId()))
                                    .document(euid)
                            )
                    ).timeout(Time.of(t -> t.time("30s")));
                }
                BulkResponse result = elasticsearchClient.bulk(br.build());
                // Log errors, if any
                if (result.errors()) {
                    log.error("UserInfo Batch save to es error");
                    for (BulkResponseItem item : result.items()) {
                        if (item.error() != null) {
                            log.error(item.error().reason());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.debug("updateEsUserInfo 执行耗时：{}", System.currentTimeMillis() - l1);
    }

    private void updateEsVideo() {
        try {
            int size = 1000;
            // 从数据库查size条数据，拼接发往es
            for (int i = 0; ; i++) {
                List<EsVideoDto> esVideoDtoList = videoService.selectBatchEsVideoDto(i, size);
                if (Collections.isEmpty(esVideoDtoList)) break;
                BulkRequest.Builder br = new BulkRequest.Builder();
                for (EsVideoDto evd : esVideoDtoList) {
                    br.operations(op -> op
                            .index(idx -> idx
                                    .index(VideoIndex.NAME)
                                    .id(String.valueOf(evd.getId()))
                                    .document(evd)
                            )
                    ).timeout(Time.of(t -> t.time("30s")));
                }

                BulkResponse result = elasticsearchClient.bulk(br.build());
                // Log errors, if any
                if (result.errors()) {
                    log.error("Video Batch save to es error");
                    for (BulkResponseItem item : result.items()) {
                        if (item.error() != null) {
                            log.error(item.error().reason());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
