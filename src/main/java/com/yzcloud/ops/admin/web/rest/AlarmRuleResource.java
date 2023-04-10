package com.yzcloud.ops.admin.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.yzcloud.ops.admin.domain.AlarmRule;
import com.yzcloud.ops.admin.repository.AlarmRuleRepository;
import com.yzcloud.ops.admin.repository.search.AlarmRuleSearchRepository;
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
 * REST controller for managing {@link com.yzcloud.ops.admin.domain.AlarmRule}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class AlarmRuleResource {

    private final Logger log = LoggerFactory.getLogger(AlarmRuleResource.class);

    private static final String ENTITY_NAME = "alarmRule";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AlarmRuleRepository alarmRuleRepository;

    private final AlarmRuleSearchRepository alarmRuleSearchRepository;

    public AlarmRuleResource(AlarmRuleRepository alarmRuleRepository, AlarmRuleSearchRepository alarmRuleSearchRepository) {
        this.alarmRuleRepository = alarmRuleRepository;
        this.alarmRuleSearchRepository = alarmRuleSearchRepository;
    }

    /**
     * {@code POST  /alarm-rules} : Create a new alarmRule.
     *
     * @param alarmRule the alarmRule to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new alarmRule, or with status {@code 400 (Bad Request)} if the alarmRule has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/alarm-rules")
    public ResponseEntity<AlarmRule> createAlarmRule(@RequestBody AlarmRule alarmRule) throws URISyntaxException {
        log.debug("REST request to save AlarmRule : {}", alarmRule);
        if (alarmRule.getId() != null) {
            throw new BadRequestAlertException("A new alarmRule cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AlarmRule result = alarmRuleRepository.save(alarmRule);
        alarmRuleSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/alarm-rules/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /alarm-rules/:id} : Updates an existing alarmRule.
     *
     * @param id the id of the alarmRule to save.
     * @param alarmRule the alarmRule to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alarmRule,
     * or with status {@code 400 (Bad Request)} if the alarmRule is not valid,
     * or with status {@code 500 (Internal Server Error)} if the alarmRule couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/alarm-rules/{id}")
    public ResponseEntity<AlarmRule> updateAlarmRule(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AlarmRule alarmRule
    ) throws URISyntaxException {
        log.debug("REST request to update AlarmRule : {}, {}", id, alarmRule);
        if (alarmRule.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alarmRule.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alarmRuleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        AlarmRule result = alarmRuleRepository.save(alarmRule);
        alarmRuleSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alarmRule.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /alarm-rules/:id} : Partial updates given fields of an existing alarmRule, field will ignore if it is null
     *
     * @param id the id of the alarmRule to save.
     * @param alarmRule the alarmRule to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alarmRule,
     * or with status {@code 400 (Bad Request)} if the alarmRule is not valid,
     * or with status {@code 404 (Not Found)} if the alarmRule is not found,
     * or with status {@code 500 (Internal Server Error)} if the alarmRule couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/alarm-rules/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AlarmRule> partialUpdateAlarmRule(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AlarmRule alarmRule
    ) throws URISyntaxException {
        log.debug("REST request to partial update AlarmRule partially : {}, {}", id, alarmRule);
        if (alarmRule.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alarmRule.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alarmRuleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AlarmRule> result = alarmRuleRepository
            .findById(alarmRule.getId())
            .map(existingAlarmRule -> {
                if (alarmRule.getName() != null) {
                    existingAlarmRule.setName(alarmRule.getName());
                }
                if (alarmRule.getDescription() != null) {
                    existingAlarmRule.setDescription(alarmRule.getDescription());
                }
                if (alarmRule.getAlarmType() != null) {
                    existingAlarmRule.setAlarmType(alarmRule.getAlarmType());
                }
                if (alarmRule.getConf() != null) {
                    existingAlarmRule.setConf(alarmRule.getConf());
                }
                if (alarmRule.getCreatedBy() != null) {
                    existingAlarmRule.setCreatedBy(alarmRule.getCreatedBy());
                }
                if (alarmRule.getCreateTime() != null) {
                    existingAlarmRule.setCreateTime(alarmRule.getCreateTime());
                }
                if (alarmRule.getModifiedBy() != null) {
                    existingAlarmRule.setModifiedBy(alarmRule.getModifiedBy());
                }
                if (alarmRule.getModifiedTime() != null) {
                    existingAlarmRule.setModifiedTime(alarmRule.getModifiedTime());
                }
                if (alarmRule.getStatus() != null) {
                    existingAlarmRule.setStatus(alarmRule.getStatus());
                }

                return existingAlarmRule;
            })
            .map(alarmRuleRepository::save)
            .map(savedAlarmRule -> {
                alarmRuleSearchRepository.save(savedAlarmRule);

                return savedAlarmRule;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alarmRule.getId().toString())
        );
    }

    /**
     * {@code GET  /alarm-rules} : get all the alarmRules.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of alarmRules in body.
     */
    @GetMapping("/alarm-rules")
    public List<AlarmRule> getAllAlarmRules() {
        log.debug("REST request to get all AlarmRules");
        return alarmRuleRepository.findAll();
    }

    /**
     * {@code GET  /alarm-rules/:id} : get the "id" alarmRule.
     *
     * @param id the id of the alarmRule to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the alarmRule, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/alarm-rules/{id}")
    public ResponseEntity<AlarmRule> getAlarmRule(@PathVariable Long id) {
        log.debug("REST request to get AlarmRule : {}", id);
        Optional<AlarmRule> alarmRule = alarmRuleRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(alarmRule);
    }

    /**
     * {@code DELETE  /alarm-rules/:id} : delete the "id" alarmRule.
     *
     * @param id the id of the alarmRule to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/alarm-rules/{id}")
    public ResponseEntity<Void> deleteAlarmRule(@PathVariable Long id) {
        log.debug("REST request to delete AlarmRule : {}", id);
        alarmRuleRepository.deleteById(id);
        alarmRuleSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/alarm-rules?query=:query} : search for the alarmRule corresponding
     * to the query.
     *
     * @param query the query of the alarmRule search.
     * @return the result of the search.
     */
    @GetMapping("/_search/alarm-rules")
    public List<AlarmRule> searchAlarmRules(@RequestParam String query) {
        log.debug("REST request to search AlarmRules for query {}", query);
        return StreamSupport.stream(alarmRuleSearchRepository.search(query).spliterator(), false).collect(Collectors.toList());
    }
}
