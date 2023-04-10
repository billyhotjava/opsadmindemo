package com.yzcloud.ops.admin.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.yzcloud.ops.admin.domain.EventRule;
import com.yzcloud.ops.admin.repository.EventRuleRepository;
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
 * Spring Data Elasticsearch repository for the {@link EventRule} entity.
 */
public interface EventRuleSearchRepository extends ElasticsearchRepository<EventRule, Long>, EventRuleSearchRepositoryInternal {}

interface EventRuleSearchRepositoryInternal {
    Stream<EventRule> search(String query);

    Stream<EventRule> search(Query query);

    void index(EventRule entity);
}

class EventRuleSearchRepositoryInternalImpl implements EventRuleSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final EventRuleRepository repository;

    EventRuleSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, EventRuleRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<EventRule> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery);
    }

    @Override
    public Stream<EventRule> search(Query query) {
        return elasticsearchTemplate.search(query, EventRule.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(EventRule entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
