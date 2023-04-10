package com.yzcloud.ops.admin.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.yzcloud.ops.admin.domain.AlarmRule;
import com.yzcloud.ops.admin.repository.AlarmRuleRepository;
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
 * Spring Data Elasticsearch repository for the {@link AlarmRule} entity.
 */
public interface AlarmRuleSearchRepository extends ElasticsearchRepository<AlarmRule, Long>, AlarmRuleSearchRepositoryInternal {}

interface AlarmRuleSearchRepositoryInternal {
    Stream<AlarmRule> search(String query);

    Stream<AlarmRule> search(Query query);

    void index(AlarmRule entity);
}

class AlarmRuleSearchRepositoryInternalImpl implements AlarmRuleSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final AlarmRuleRepository repository;

    AlarmRuleSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, AlarmRuleRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<AlarmRule> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery);
    }

    @Override
    public Stream<AlarmRule> search(Query query) {
        return elasticsearchTemplate.search(query, AlarmRule.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(AlarmRule entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
