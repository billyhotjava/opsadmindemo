package com.yzcloud.ops.admin.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.yzcloud.ops.admin.domain.AlarmLevel;
import com.yzcloud.ops.admin.repository.AlarmLevelRepository;
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
 * Spring Data Elasticsearch repository for the {@link AlarmLevel} entity.
 */
public interface AlarmLevelSearchRepository extends ElasticsearchRepository<AlarmLevel, Long>, AlarmLevelSearchRepositoryInternal {}

interface AlarmLevelSearchRepositoryInternal {
    Stream<AlarmLevel> search(String query);

    Stream<AlarmLevel> search(Query query);

    void index(AlarmLevel entity);
}

class AlarmLevelSearchRepositoryInternalImpl implements AlarmLevelSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final AlarmLevelRepository repository;

    AlarmLevelSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, AlarmLevelRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<AlarmLevel> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery);
    }

    @Override
    public Stream<AlarmLevel> search(Query query) {
        return elasticsearchTemplate.search(query, AlarmLevel.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(AlarmLevel entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
