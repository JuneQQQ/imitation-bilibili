package io.juneqqq.dao.repository;


import io.juneqqq.dao.entity.Video;
import io.juneqqq.dao.repository.esmodel.EsVideoDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoDtoRepository extends ElasticsearchRepository<EsVideoDto, Long> {

    EsVideoDto findByTitleLike(String keyword);
}
