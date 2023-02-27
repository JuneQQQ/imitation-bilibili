package io.juneqqq.pojo.dao.repository;

import io.juneqqq.pojo.dao.repository.esmodel.EsUserInfoDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserInfoDtoRepository extends ElasticsearchRepository<EsUserInfoDto, Long> {
    EsUserInfoDto searchByUserId(Long userId);

    void deleteByUserId(Long userId);
}
