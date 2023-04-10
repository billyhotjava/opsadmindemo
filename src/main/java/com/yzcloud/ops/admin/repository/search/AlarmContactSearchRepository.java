package com.yzcloud.ops.admin.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.yzcloud.ops.admin.domain.AlarmContact;
import com.yzcloud.ops.admin.repository.AlarmContactRepository;
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
 * Spring Data Elasticsearch repository for the {@link AlarmContact} entity.
 */
public interface AlarmContactSearchRepository extends ElasticsearchRepository<AlarmContact, Long>, AlarmContactSearchRepositoryInternal {}

interface AlarmContactSearchRepositoryInternal {
    Stream<AlarmContact> search(String query);

    Stream<AlarmContact> search(Query query);

    void index(AlarmContact entity);
}

class AlarmContactSearchRepositoryInternalImpl implements AlarmContactSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final AlarmContactRepository repository;

    AlarmContactSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, AlarmContactRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<AlarmContact> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery);
    }

    @Override
    public Stream<AlarmContact> search(Query query) {
        return elasticsearchTemplate.search(query, AlarmContact.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(AlarmContact entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
