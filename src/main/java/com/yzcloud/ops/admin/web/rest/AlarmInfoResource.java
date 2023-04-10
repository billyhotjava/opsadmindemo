package com.yzcloud.ops.admin.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.yzcloud.ops.admin.domain.AlarmInfo;
import com.yzcloud.ops.admin.repository.AlarmInfoRepository;
import com.yzcloud.ops.admin.repository.search.AlarmInfoSearchRepository;
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
 * REST controller for managing {@link com.yzcloud.ops.admin.domain.AlarmInfo}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class AlarmInfoResource {

    private final Logger log = LoggerFactory.getLogger(AlarmInfoResource.class);

    private static final String ENTITY_NAME = "alarmInfo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AlarmInfoRepository alarmInfoRepository;

    private final AlarmInfoSearchRepository alarmInfoSearchRepository;

    public AlarmInfoResource(AlarmInfoRepository alarmInfoRepository, AlarmInfoSearchRepository alarmInfoSearchRepository) {
        this.alarmInfoRepository = alarmInfoRepository;
        this.alarmInfoSearchRepository = alarmInfoSearchRepository;
    }

    /**
     * {@code POST  /alarm-infos} : Create a new alarmInfo.
     *
     * @param alarmInfo the alarmInfo to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new alarmInfo, or with status {@code 400 (Bad Request)} if the alarmInfo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/alarm-infos")
    public ResponseEntity<AlarmInfo> createAlarmInfo(@RequestBody AlarmInfo alarmInfo) throws URISyntaxException {
        log.debug("REST request to save AlarmInfo : {}", alarmInfo);
        if (alarmInfo.getId() != null) {
            throw new BadRequestAlertException("A new alarmInfo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AlarmInfo result = alarmInfoRepository.save(alarmInfo);
        alarmInfoSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/alarm-infos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /alarm-infos/:id} : Updates an existing alarmInfo.
     *
     * @param id the id of the alarmInfo to save.
     * @param alarmInfo the alarmInfo to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alarmInfo,
     * or with status {@code 400 (Bad Request)} if the alarmInfo is not valid,
     * or with status {@code 500 (Internal Server Error)} if the alarmInfo couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/alarm-infos/{id}")
    public ResponseEntity<AlarmInfo> updateAlarmInfo(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AlarmInfo alarmInfo
    ) throws URISyntaxException {
        log.debug("REST request to update AlarmInfo : {}, {}", id, alarmInfo);
        if (alarmInfo.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alarmInfo.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alarmInfoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        AlarmInfo result = alarmInfoRepository.save(alarmInfo);
        alarmInfoSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alarmInfo.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /alarm-infos/:id} : Partial updates given fields of an existing alarmInfo, field will ignore if it is null
     *
     * @param id the id of the alarmInfo to save.
     * @param alarmInfo the alarmInfo to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alarmInfo,
     * or with status {@code 400 (Bad Request)} if the alarmInfo is not valid,
     * or with status {@code 404 (Not Found)} if the alarmInfo is not found,
     * or with status {@code 500 (Internal Server Error)} if the alarmInfo couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/alarm-infos/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AlarmInfo> partialUpdateAlarmInfo(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AlarmInfo alarmInfo
    ) throws URISyntaxException {
        log.debug("REST request to partial update AlarmInfo partially : {}, {}", id, alarmInfo);
        if (alarmInfo.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alarmInfo.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alarmInfoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AlarmInfo> result = alarmInfoRepository
            .findById(alarmInfo.getId())
            .map(existingAlarmInfo -> {
                if (alarmInfo.getUuid() != null) {
                    existingAlarmInfo.setUuid(alarmInfo.getUuid());
                }
                if (alarmInfo.getName() != null) {
                    existingAlarmInfo.setName(alarmInfo.getName());
                }
                if (alarmInfo.getInfo() != null) {
                    existingAlarmInfo.setInfo(alarmInfo.getInfo());
                }
                if (alarmInfo.getChecked() != null) {
                    existingAlarmInfo.setChecked(alarmInfo.getChecked());
                }
                if (alarmInfo.getCreatedTime() != null) {
                    existingAlarmInfo.setCreatedTime(alarmInfo.getCreatedTime());
                }
                if (alarmInfo.getCreatedBy() != null) {
                    existingAlarmInfo.setCreatedBy(alarmInfo.getCreatedBy());
                }
                if (alarmInfo.getModifiedTime() != null) {
                    existingAlarmInfo.setModifiedTime(alarmInfo.getModifiedTime());
                }
                if (alarmInfo.getModifiedBy() != null) {
                    existingAlarmInfo.setModifiedBy(alarmInfo.getModifiedBy());
                }
                if (alarmInfo.getAlertTime() != null) {
                    existingAlarmInfo.setAlertTime(alarmInfo.getAlertTime());
                }
                if (alarmInfo.getAlertDoc() != null) {
                    existingAlarmInfo.setAlertDoc(alarmInfo.getAlertDoc());
                }
                if (alarmInfo.getEsDocumentId() != null) {
                    existingAlarmInfo.setEsDocumentId(alarmInfo.getEsDocumentId());
                }
                if (alarmInfo.getEsIndexName() != null) {
                    existingAlarmInfo.setEsIndexName(alarmInfo.getEsIndexName());
                }

                return existingAlarmInfo;
            })
            .map(alarmInfoRepository::save)
            .map(savedAlarmInfo -> {
                alarmInfoSearchRepository.save(savedAlarmInfo);

                return savedAlarmInfo;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alarmInfo.getId().toString())
        );
    }

    /**
     * {@code GET  /alarm-infos} : get all the alarmInfos.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of alarmInfos in body.
     */
    @GetMapping("/alarm-infos")
    public List<AlarmInfo> getAllAlarmInfos() {
        log.debug("REST request to get all AlarmInfos");
        return alarmInfoRepository.findAll();
    }

    /**
     * {@code GET  /alarm-infos/:id} : get the "id" alarmInfo.
     *
     * @param id the id of the alarmInfo to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the alarmInfo, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/alarm-infos/{id}")
    public ResponseEntity<AlarmInfo> getAlarmInfo(@PathVariable Long id) {
        log.debug("REST request to get AlarmInfo : {}", id);
        Optional<AlarmInfo> alarmInfo = alarmInfoRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(alarmInfo);
    }

    /**
     * {@code DELETE  /alarm-infos/:id} : delete the "id" alarmInfo.
     *
     * @param id the id of the alarmInfo to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/alarm-infos/{id}")
    public ResponseEntity<Void> deleteAlarmInfo(@PathVariable Long id) {
        log.debug("REST request to delete AlarmInfo : {}", id);
        alarmInfoRepository.deleteById(id);
        alarmInfoSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/alarm-infos?query=:query} : search for the alarmInfo corresponding
     * to the query.
     *
     * @param query the query of the alarmInfo search.
     * @return the result of the search.
     */
    @GetMapping("/_search/alarm-infos")
    public List<AlarmInfo> searchAlarmInfos(@RequestParam String query) {
        log.debug("REST request to search AlarmInfos for query {}", query);
        return StreamSupport.stream(alarmInfoSearchRepository.search(query).spliterator(), false).collect(Collectors.toList());
    }

    /**
     * added manually
     */
    @GetMapping(value = { "/query-alarm-infos" })
    public List<AlarmInfo> getAllAlarmInfosByQuery(
        @RequestParam(value = "alarmruleid", required = false) Long alarmRuleid,
        @RequestParam(value = "eventid", required = false) Long eventId,
        @RequestParam(value = "categaryid", required = false) Long categoryId
    ) {
        List<AlarmInfo> alarmInfoList = this.alarmInfoRepository.findAllAlarmInfoByQuery(alarmRuleid, eventId, categoryId);
        log.debug("============  query alarm infos ========:" + alarmInfoList.size());
        return alarmInfoList;
    }
}
