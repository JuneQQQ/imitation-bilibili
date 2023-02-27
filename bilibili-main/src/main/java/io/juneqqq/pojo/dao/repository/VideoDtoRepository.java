package io.juneqqq.pojo.dao.repository;


import io.juneqqq.pojo.dao.repository.esmodel.EsVideoDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoDtoRepository extends ElasticsearchRepository<EsVideoDto, Long> {

    EsVideoDto findByTitleLike(String keyword);
}
