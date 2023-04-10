package com.yzcloud.ops.admin.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.yzcloud.ops.admin.domain.AlarmInfo;
import com.yzcloud.ops.admin.repository.AlarmInfoRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data Elasticsearch repository for the {@link AlarmInfo} entity.
 */
public interface AlarmInfoSearchRepository extends ElasticsearchRepository<AlarmInfo, Long>, AlarmInfoSearchRepositoryInternal {}

interface AlarmInfoSearchRepositoryInternal {
    Stream<AlarmInfo> search(String query);

    Stream<AlarmInfo> search(Query query);

    void index(AlarmInfo entity);
}

class AlarmInfoSearchRepositoryInternalImpl implements AlarmInfoSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final AlarmInfoRepository repository;

    AlarmInfoSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, AlarmInfoRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<AlarmInfo> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery);
    }

    @Override
    public Stream<AlarmInfo> search(Query query) {
        return elasticsearchTemplate.search(query, AlarmInfo.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(AlarmInfo entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
