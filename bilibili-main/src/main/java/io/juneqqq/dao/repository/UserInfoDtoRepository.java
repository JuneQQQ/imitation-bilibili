package io.juneqqq.dao.repository;

import io.juneqqq.dao.repository.esmodel.EsUserInfoDto;
import io.juneqqq.dao.repository.esmodel.EsVideoDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserInfoDtoRepository extends ElasticsearchRepository<EsUserInfoDto, Long> {
    EsUserInfoDto searchByUserId(Long userId);

    void deleteByUserId(Long userId);
}
