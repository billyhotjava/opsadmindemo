package com.yzcloud.ops.admin.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.yzcloud.ops.admin.domain.EventRule;
import com.yzcloud.ops.admin.repository.EventRuleRepository;
import com.yzcloud.ops.admin.repository.search.EventRuleSearchRepository;
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
 * REST controller for managing {@link com.yzcloud.ops.admin.domain.EventRule}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class EventRuleResource {

    private final Logger log = LoggerFactory.getLogger(EventRuleResource.class);

    private static final String ENTITY_NAME = "eventRule";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventRuleRepository eventRuleRepository;

    private final EventRuleSearchRepository eventRuleSearchRepository;

    public EventRuleResource(EventRuleRepository eventRuleRepository, EventRuleSearchRepository eventRuleSearchRepository) {
        this.eventRuleRepository = eventRuleRepository;
        this.eventRuleSearchRepository = eventRuleSearchRepository;
    }

    /**
     * {@code POST  /event-rules} : Create a new eventRule.
     *
     * @param eventRule the eventRule to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eventRule, or with status {@code 400 (Bad Request)} if the eventRule has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/event-rules")
    public ResponseEntity<EventRule> createEventRule(@RequestBody EventRule eventRule) throws URISyntaxException {
        log.debug("REST request to save EventRule : {}", eventRule);
        if (eventRule.getId() != null) {
            throw new BadRequestAlertException("A new eventRule cannot already have an ID", ENTITY_NAME, "idexists");
        }
        EventRule result = eventRuleRepository.save(eventRule);
        eventRuleSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/event-rules/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /event-rules/:id} : Updates an existing eventRule.
     *
     * @param id the id of the eventRule to save.
     * @param eventRule the eventRule to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventRule,
     * or with status {@code 400 (Bad Request)} if the eventRule is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eventRule couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/event-rules/{id}")
    public ResponseEntity<EventRule> updateEventRule(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody EventRule eventRule
    ) throws URISyntaxException {
        log.debug("REST request to update EventRule : {}, {}", id, eventRule);
        if (eventRule.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventRule.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventRuleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        EventRule result = eventRuleRepository.save(eventRule);
        eventRuleSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, eventRule.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /event-rules/:id} : Partial updates given fields of an existing eventRule, field will ignore if it is null
     *
     * @param id the id of the eventRule to save.
     * @param eventRule the eventRule to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventRule,
     * or with status {@code 400 (Bad Request)} if the eventRule is not valid,
     * or with status {@code 404 (Not Found)} if the eventRule is not found,
     * or with status {@code 500 (Internal Server Error)} if the eventRule couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/event-rules/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EventRule> partialUpdateEventRule(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody EventRule eventRule
    ) throws URISyntaxException {
        log.debug("REST request to partial update EventRule partially : {}, {}", id, eventRule);
        if (eventRule.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventRule.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventRuleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EventRule> result = eventRuleRepository
            .findById(eventRule.getId())
            .map(existingEventRule -> {
                if (eventRule.getName() != null) {
                    existingEventRule.setName(eventRule.getName());
                }
                if (eventRule.getAlias() != null) {
                    existingEventRule.setAlias(eventRule.getAlias());
                }
                if (eventRule.getEventType() != null) {
                    existingEventRule.setEventType(eventRule.getEventType());
                }
                if (eventRule.getDescription() != null) {
                    existingEventRule.setDescription(eventRule.getDescription());
                }
                if (eventRule.getEventSample() != null) {
                    existingEventRule.setEventSample(eventRule.getEventSample());
                }
                if (eventRule.getEventRule() != null) {
                    existingEventRule.setEventRule(eventRule.getEventRule());
                }
                if (eventRule.getCreatedTime() != null) {
                    existingEventRule.setCreatedTime(eventRule.getCreatedTime());
                }
                if (eventRule.getCreatedBy() != null) {
                    existingEventRule.setCreatedBy(eventRule.getCreatedBy());
                }
                if (eventRule.getModifiedTime() != null) {
                    existingEventRule.setModifiedTime(eventRule.getModifiedTime());
                }
                if (eventRule.getModifiedBy() != null) {
                    existingEventRule.setModifiedBy(eventRule.getModifiedBy());
                }

                return existingEventRule;
            })
            .map(eventRuleRepository::save)
            .map(savedEventRule -> {
                eventRuleSearchRepository.save(savedEventRule);

                return savedEventRule;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, eventRule.getId().toString())
        );
    }

    /**
     * {@code GET  /event-rules} : get all the eventRules.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eventRules in body.
     */
    @GetMapping("/event-rules")
    public List<EventRule> getAllEventRules() {
        log.debug("REST request to get all EventRules");
        return eventRuleRepository.findAll();
    }

    /**
     * {@code GET  /event-rules/:id} : get the "id" eventRule.
     *
     * @param id the id of the eventRule to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eventRule, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/event-rules/{id}")
    public ResponseEntity<EventRule> getEventRule(@PathVariable Long id) {
        log.debug("REST request to get EventRule : {}", id);
        Optional<EventRule> eventRule = eventRuleRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(eventRule);
    }

    /**
     * {@code DELETE  /event-rules/:id} : delete the "id" eventRule.
     *
     * @param id the id of the eventRule to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/event-rules/{id}")
    public ResponseEntity<Void> deleteEventRule(@PathVariable Long id) {
        log.debug("REST request to delete EventRule : {}", id);
        eventRuleRepository.deleteById(id);
        eventRuleSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/event-rules?query=:query} : search for the eventRule corresponding
     * to the query.
     *
     * @param query the query of the eventRule search.
     * @return the result of the search.
     */
    @GetMapping("/_search/event-rules")
    public List<EventRule> searchEventRules(@RequestParam String query) {
        log.debug("REST request to search EventRules for query {}", query);
        return StreamSupport.stream(eventRuleSearchRepository.search(query).spliterator(), false).collect(Collectors.toList());
    }
}
