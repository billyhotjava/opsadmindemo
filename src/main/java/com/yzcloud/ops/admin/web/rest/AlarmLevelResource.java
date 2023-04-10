package com.yzcloud.ops.admin.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.yzcloud.ops.admin.domain.AlarmLevel;
import com.yzcloud.ops.admin.repository.AlarmLevelRepository;
import com.yzcloud.ops.admin.repository.search.AlarmLevelSearchRepository;
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
 * REST controller for managing {@link com.yzcloud.ops.admin.domain.AlarmLevel}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class AlarmLevelResource {

    private final Logger log = LoggerFactory.getLogger(AlarmLevelResource.class);

    private static final String ENTITY_NAME = "alarmLevel";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AlarmLevelRepository alarmLevelRepository;

    private final AlarmLevelSearchRepository alarmLevelSearchRepository;

    public AlarmLevelResource(AlarmLevelRepository alarmLevelRepository, AlarmLevelSearchRepository alarmLevelSearchRepository) {
        this.alarmLevelRepository = alarmLevelRepository;
        this.alarmLevelSearchRepository = alarmLevelSearchRepository;
    }

    /**
     * {@code POST  /alarm-levels} : Create a new alarmLevel.
     *
     * @param alarmLevel the alarmLevel to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new alarmLevel, or with status {@code 400 (Bad Request)} if the alarmLevel has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/alarm-levels")
    public ResponseEntity<AlarmLevel> createAlarmLevel(@RequestBody AlarmLevel alarmLevel) throws URISyntaxException {
        log.debug("REST request to save AlarmLevel : {}", alarmLevel);
        if (alarmLevel.getId() != null) {
            throw new BadRequestAlertException("A new alarmLevel cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AlarmLevel result = alarmLevelRepository.save(alarmLevel);
        alarmLevelSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/alarm-levels/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /alarm-levels/:id} : Updates an existing alarmLevel.
     *
     * @param id the id of the alarmLevel to save.
     * @param alarmLevel the alarmLevel to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alarmLevel,
     * or with status {@code 400 (Bad Request)} if the alarmLevel is not valid,
     * or with status {@code 500 (Internal Server Error)} if the alarmLevel couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/alarm-levels/{id}")
    public ResponseEntity<AlarmLevel> updateAlarmLevel(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AlarmLevel alarmLevel
    ) throws URISyntaxException {
        log.debug("REST request to update AlarmLevel : {}, {}", id, alarmLevel);
        if (alarmLevel.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alarmLevel.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alarmLevelRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        AlarmLevel result = alarmLevelRepository.save(alarmLevel);
        alarmLevelSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alarmLevel.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /alarm-levels/:id} : Partial updates given fields of an existing alarmLevel, field will ignore if it is null
     *
     * @param id the id of the alarmLevel to save.
     * @param alarmLevel the alarmLevel to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alarmLevel,
     * or with status {@code 400 (Bad Request)} if the alarmLevel is not valid,
     * or with status {@code 404 (Not Found)} if the alarmLevel is not found,
     * or with status {@code 500 (Internal Server Error)} if the alarmLevel couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/alarm-levels/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AlarmLevel> partialUpdateAlarmLevel(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AlarmLevel alarmLevel
    ) throws URISyntaxException {
        log.debug("REST request to partial update AlarmLevel partially : {}, {}", id, alarmLevel);
        if (alarmLevel.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alarmLevel.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alarmLevelRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AlarmLevel> result = alarmLevelRepository
            .findById(alarmLevel.getId())
            .map(existingAlarmLevel -> {
                if (alarmLevel.getName() != null) {
                    existingAlarmLevel.setName(alarmLevel.getName());
                }
                if (alarmLevel.getColor() != null) {
                    existingAlarmLevel.setColor(alarmLevel.getColor());
                }
                if (alarmLevel.getDescription() != null) {
                    existingAlarmLevel.setDescription(alarmLevel.getDescription());
                }

                return existingAlarmLevel;
            })
            .map(alarmLevelRepository::save)
            .map(savedAlarmLevel -> {
                alarmLevelSearchRepository.save(savedAlarmLevel);

                return savedAlarmLevel;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alarmLevel.getId().toString())
        );
    }

    /**
     * {@code GET  /alarm-levels} : get all the alarmLevels.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of alarmLevels in body.
     */
    @GetMapping("/alarm-levels")
    public List<AlarmLevel> getAllAlarmLevels() {
        log.debug("REST request to get all AlarmLevels");
        return alarmLevelRepository.findAll();
    }

    /**
     * {@code GET  /alarm-levels/:id} : get the "id" alarmLevel.
     *
     * @param id the id of the alarmLevel to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the alarmLevel, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/alarm-levels/{id}")
    public ResponseEntity<AlarmLevel> getAlarmLevel(@PathVariable Long id) {
        log.debug("REST request to get AlarmLevel : {}", id);
        Optional<AlarmLevel> alarmLevel = alarmLevelRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(alarmLevel);
    }

    /**
     * {@code DELETE  /alarm-levels/:id} : delete the "id" alarmLevel.
     *
     * @param id the id of the alarmLevel to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/alarm-levels/{id}")
    public ResponseEntity<Void> deleteAlarmLevel(@PathVariable Long id) {
        log.debug("REST request to delete AlarmLevel : {}", id);
        alarmLevelRepository.deleteById(id);
        alarmLevelSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/alarm-levels?query=:query} : search for the alarmLevel corresponding
     * to the query.
     *
     * @param query the query of the alarmLevel search.
     * @return the result of the search.
     */
    @GetMapping("/_search/alarm-levels")
    public List<AlarmLevel> searchAlarmLevels(@RequestParam String query) {
        log.debug("REST request to search AlarmLevels for query {}", query);
        return StreamSupport.stream(alarmLevelSearchRepository.search(query).spliterator(), false).collect(Collectors.toList());
    }
}
