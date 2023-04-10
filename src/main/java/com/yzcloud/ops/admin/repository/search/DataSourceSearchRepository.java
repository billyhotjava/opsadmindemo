package com.yzcloud.ops.admin.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.yzcloud.ops.admin.domain.DataSource;
import com.yzcloud.ops.admin.repository.DataSourceRepository;
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
 * Spring Data Elasticsearch repository for the {@link DataSource} entity.
 */
public interface DataSourceSearchRepository extends ElasticsearchRepository<DataSource, Long>, DataSourceSearchRepositoryInternal {}

interface DataSourceSearchRepositoryInternal {
    Stream<DataSource> search(String query);

    Stream<DataSource> search(Query query);

    void index(DataSource entity);
}

class DataSourceSearchRepositoryInternalImpl implements DataSourceSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final DataSourceRepository repository;

    DataSourceSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, DataSourceRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<DataSource> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery);
    }

    @Override
    public Stream<DataSource> search(Query query) {
        return elasticsearchTemplate.search(query, DataSource.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(DataSource entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
