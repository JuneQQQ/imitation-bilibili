package io.juneqqq.dao.repository;

import io.juneqqq.dao.repository.esmodel.EsUserInfoDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserInfoDtoRepository extends ElasticsearchRepository<EsUserInfoDto, Long> {

}
