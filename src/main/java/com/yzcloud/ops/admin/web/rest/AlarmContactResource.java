package com.yzcloud.ops.admin.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.yzcloud.ops.admin.domain.AlarmContact;
import com.yzcloud.ops.admin.repository.AlarmContactRepository;
import com.yzcloud.ops.admin.repository.search.AlarmContactSearchRepository;
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
 * REST controller for managing {@link com.yzcloud.ops.admin.domain.AlarmContact}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class AlarmContactResource {

    private final Logger log = LoggerFactory.getLogger(AlarmContactResource.class);

    private static final String ENTITY_NAME = "alarmContact";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AlarmContactRepository alarmContactRepository;

    private final AlarmContactSearchRepository alarmContactSearchRepository;

    public AlarmContactResource(AlarmContactRepository alarmContactRepository, AlarmContactSearchRepository alarmContactSearchRepository) {
        this.alarmContactRepository = alarmContactRepository;
        this.alarmContactSearchRepository = alarmContactSearchRepository;
    }

    /**
     * {@code POST  /alarm-contacts} : Create a new alarmContact.
     *
     * @param alarmContact the alarmContact to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new alarmContact, or with status {@code 400 (Bad Request)} if the alarmContact has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/alarm-contacts")
    public ResponseEntity<AlarmContact> createAlarmContact(@RequestBody AlarmContact alarmContact) throws URISyntaxException {
        log.debug("REST request to save AlarmContact : {}", alarmContact);
        if (alarmContact.getId() != null) {
            throw new BadRequestAlertException("A new alarmContact cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AlarmContact result = alarmContactRepository.save(alarmContact);
        alarmContactSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/alarm-contacts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /alarm-contacts/:id} : Updates an existing alarmContact.
     *
     * @param id the id of the alarmContact to save.
     * @param alarmContact the alarmContact to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alarmContact,
     * or with status {@code 400 (Bad Request)} if the alarmContact is not valid,
     * or with status {@code 500 (Internal Server Error)} if the alarmContact couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/alarm-contacts/{id}")
    public ResponseEntity<AlarmContact> updateAlarmContact(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AlarmContact alarmContact
    ) throws URISyntaxException {
        log.debug("REST request to update AlarmContact : {}, {}", id, alarmContact);
        if (alarmContact.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alarmContact.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alarmContactRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        AlarmContact result = alarmContactRepository.save(alarmContact);
        alarmContactSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alarmContact.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /alarm-contacts/:id} : Partial updates given fields of an existing alarmContact, field will ignore if it is null
     *
     * @param id the id of the alarmContact to save.
     * @param alarmContact the alarmContact to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alarmContact,
     * or with status {@code 400 (Bad Request)} if the alarmContact is not valid,
     * or with status {@code 404 (Not Found)} if the alarmContact is not found,
     * or with status {@code 500 (Internal Server Error)} if the alarmContact couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/alarm-contacts/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AlarmContact> partialUpdateAlarmContact(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AlarmContact alarmContact
    ) throws URISyntaxException {
        log.debug("REST request to partial update AlarmContact partially : {}, {}", id, alarmContact);
        if (alarmContact.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alarmContact.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alarmContactRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AlarmContact> result = alarmContactRepository
            .findById(alarmContact.getId())
            .map(existingAlarmContact -> {
                if (alarmContact.getTitle() != null) {
                    existingAlarmContact.setTitle(alarmContact.getTitle());
                }
                if (alarmContact.getName() != null) {
                    existingAlarmContact.setName(alarmContact.getName());
                }
                if (alarmContact.getContent() != null) {
                    existingAlarmContact.setContent(alarmContact.getContent());
                }
                if (alarmContact.getType() != null) {
                    existingAlarmContact.setType(alarmContact.getType());
                }
                if (alarmContact.getContactWay() != null) {
                    existingAlarmContact.setContactWay(alarmContact.getContactWay());
                }
                if (alarmContact.getCreatedTime() != null) {
                    existingAlarmContact.setCreatedTime(alarmContact.getCreatedTime());
                }
                if (alarmContact.getCreatedBy() != null) {
                    existingAlarmContact.setCreatedBy(alarmContact.getCreatedBy());
                }
                if (alarmContact.getModifiedTime() != null) {
                    existingAlarmContact.setModifiedTime(alarmContact.getModifiedTime());
                }
                if (alarmContact.getModifiedBy() != null) {
                    existingAlarmContact.setModifiedBy(alarmContact.getModifiedBy());
                }

                return existingAlarmContact;
            })
            .map(alarmContactRepository::save)
            .map(savedAlarmContact -> {
                alarmContactSearchRepository.save(savedAlarmContact);

                return savedAlarmContact;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alarmContact.getId().toString())
        );
    }

    /**
     * {@code GET  /alarm-contacts} : get all the alarmContacts.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of alarmContacts in body.
     */
    @GetMapping("/alarm-contacts")
    public List<AlarmContact> getAllAlarmContacts() {
        log.debug("REST request to get all AlarmContacts");
        return alarmContactRepository.findAll();
    }

    /**
     * {@code GET  /alarm-contacts/:id} : get the "id" alarmContact.
     *
     * @param id the id of the alarmContact to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the alarmContact, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/alarm-contacts/{id}")
    public ResponseEntity<AlarmContact> getAlarmContact(@PathVariable Long id) {
        log.debug("REST request to get AlarmContact : {}", id);
        Optional<AlarmContact> alarmContact = alarmContactRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(alarmContact);
    }

    /**
     * {@code DELETE  /alarm-contacts/:id} : delete the "id" alarmContact.
     *
     * @param id the id of the alarmContact to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/alarm-contacts/{id}")
    public ResponseEntity<Void> deleteAlarmContact(@PathVariable Long id) {
        log.debug("REST request to delete AlarmContact : {}", id);
        alarmContactRepository.deleteById(id);
        alarmContactSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/alarm-contacts?query=:query} : search for the alarmContact corresponding
     * to the query.
     *
     * @param query the query of the alarmContact search.
     * @return the result of the search.
     */
    @GetMapping("/_search/alarm-contacts")
    public List<AlarmContact> searchAlarmContacts(@RequestParam String query) {
        log.debug("REST request to search AlarmContacts for query {}", query);
        return StreamSupport.stream(alarmContactSearchRepository.search(query).spliterator(), false).collect(Collectors.toList());
    }
}
