package com.yzcloud.ops.admin.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.yzcloud.ops.admin.domain.DataSource;
import com.yzcloud.ops.admin.repository.DataSourceRepository;
import com.yzcloud.ops.admin.repository.search.DataSourceSearchRepository;
import com.yzcloud.ops.admin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.yzcloud.ops.admin.domain.DataSource}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class DataSourceResource {

    private final Logger log = LoggerFactory.getLogger(DataSourceResource.class);

    private static final String ENTITY_NAME = "dataSource";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DataSourceRepository dataSourceRepository;

    private final DataSourceSearchRepository dataSourceSearchRepository;

    public DataSourceResource(DataSourceRepository dataSourceRepository, DataSourceSearchRepository dataSourceSearchRepository) {
        this.dataSourceRepository = dataSourceRepository;
        this.dataSourceSearchRepository = dataSourceSearchRepository;
    }

    /**
     * {@code POST  /data-sources} : Create a new dataSource.
     *
     * @param dataSource the dataSource to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new dataSource, or with status {@code 400 (Bad Request)} if the dataSource has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/data-sources")
    public ResponseEntity<DataSource> createDataSource(@RequestBody DataSource dataSource) throws URISyntaxException {
        log.debug("REST request to save DataSource : {}", dataSource);
        if (dataSource.getId() != null) {
            throw new BadRequestAlertException("A new dataSource cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DataSource result = dataSourceRepository.save(dataSource);
        dataSourceSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/data-sources/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /data-sources/:id} : Updates an existing dataSource.
     *
     * @param id the id of the dataSource to save.
     * @param dataSource the dataSource to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dataSource,
     * or with status {@code 400 (Bad Request)} if the dataSource is not valid,
     * or with status {@code 500 (Internal Server Error)} if the dataSource couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/data-sources/{id}")
    public ResponseEntity<DataSource> updateDataSource(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody DataSource dataSource
    ) throws URISyntaxException {
        log.debug("REST request to update DataSource : {}, {}", id, dataSource);
        if (dataSource.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dataSource.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dataSourceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        DataSource result = dataSourceRepository.save(dataSource);
        dataSourceSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dataSource.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /data-sources/:id} : Partial updates given fields of an existing dataSource, field will ignore if it is null
     *
     * @param id the id of the dataSource to save.
     * @param dataSource the dataSource to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dataSource,
     * or with status {@code 400 (Bad Request)} if the dataSource is not valid,
     * or with status {@code 404 (Not Found)} if the dataSource is not found,
     * or with status {@code 500 (Internal Server Error)} if the dataSource couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/data-sources/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DataSource> partialUpdateDataSource(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody DataSource dataSource
    ) throws URISyntaxException {
        log.debug("REST request to partial update DataSource partially : {}, {}", id, dataSource);
        if (dataSource.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dataSource.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dataSourceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DataSource> result = dataSourceRepository
            .findById(dataSource.getId())
            .map(existingDataSource -> {
                if (dataSource.getName() != null) {
                    existingDataSource.setName(dataSource.getName());
                }
                if (dataSource.getAlias() != null) {
                    existingDataSource.setAlias(dataSource.getAlias());
                }
                if (dataSource.getSourceType() != null) {
                    existingDataSource.setSourceType(dataSource.getSourceType());
                }
                if (dataSource.getDescription() != null) {
                    existingDataSource.setDescription(dataSource.getDescription());
                }
                if (dataSource.getUrl() != null) {
                    existingDataSource.setUrl(dataSource.getUrl());
                }
                if (dataSource.getCredential() != null) {
                    existingDataSource.setCredential(dataSource.getCredential());
                }
                if (dataSource.getCreatedTime() != null) {
                    existingDataSource.setCreatedTime(dataSource.getCreatedTime());
                }
                if (dataSource.getCreatedBy() != null) {
                    existingDataSource.setCreatedBy(dataSource.getCreatedBy());
                }
                if (dataSource.getModifiedTime() != null) {
                    existingDataSource.setModifiedTime(dataSource.getModifiedTime());
                }
                if (dataSource.getModifiedBy() != null) {
                    existingDataSource.setModifiedBy(dataSource.getModifiedBy());
                }
                if (dataSource.getIsUsed() != null) {
                    existingDataSource.setIsUsed(dataSource.getIsUsed());
                }

                return existingDataSource;
            })
            .map(dataSourceRepository::save)
            .map(savedDataSource -> {
                dataSourceSearchRepository.save(savedDataSource);

                return savedDataSource;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dataSource.getId().toString())
        );
    }

    /**
     * {@code GET  /data-sources} : get all the dataSources.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of dataSources in body.
     */
    @GetMapping("/data-sources")
    public List<DataSource> getAllDataSources() {
        log.debug("REST request to get all DataSources");
        return dataSourceRepository.findAll();
    }

    /**
     * {@code GET  /data-sources/:id} : get the "id" dataSource.
     *
     * @param id the id of the dataSource to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the dataSource, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/data-sources/{id}")
    public ResponseEntity<DataSource> getDataSource(@PathVariable Long id) {
        log.debug("REST request to get DataSource : {}", id);
        Optional<DataSource> dataSource = dataSourceRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(dataSource);
    }

    /**
     * {@code DELETE  /data-sources/:id} : delete the "id" dataSource.
     *
     * @param id the id of the dataSource to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/data-sources/{id}")
    public ResponseEntity<Void> deleteDataSource(@PathVariable Long id) {
        log.debug("REST request to delete DataSource : {}", id);
        dataSourceRepository.deleteById(id);
        dataSourceSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/data-sources?query=:query} : search for the dataSource corresponding
     * to the query.
     *
     * @param query the query of the dataSource search.
     * @return the result of the search.
     */
    @GetMapping("/_search/data-sources")
    public List<DataSource> searchDataSources(@RequestParam String query) {
        log.debug("REST request to search DataSources for query {}", query);
        return StreamSupport.stream(dataSourceSearchRepository.search(query).spliterator(), false).collect(Collectors.toList());
    }
}
