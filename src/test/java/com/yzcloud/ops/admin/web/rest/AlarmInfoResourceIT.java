package com.yzcloud.ops.admin.web.rest;

import static com.yzcloud.ops.admin.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.yzcloud.ops.admin.IntegrationTest;
import com.yzcloud.ops.admin.domain.AlarmInfo;
import com.yzcloud.ops.admin.repository.AlarmInfoRepository;
import com.yzcloud.ops.admin.repository.search.AlarmInfoSearchRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AlarmInfoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AlarmInfoResourceIT {

    private static final String DEFAULT_UUID = "AAAAAAAAAA";
    private static final String UPDATED_UUID = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_INFO = "AAAAAAAAAA";
    private static final String UPDATED_INFO = "BBBBBBBBBB";

    private static final Boolean DEFAULT_CHECKED = false;
    private static final Boolean UPDATED_CHECKED = true;

    private static final ZonedDateTime DEFAULT_CREATED_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_MODIFIED_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_MODIFIED_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_MODIFIED_BY = "AAAAAAAAAA";
    private static final String UPDATED_MODIFIED_BY = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_ALERT_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_ALERT_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_ALERT_DOC = "AAAAAAAAAA";
    private static final String UPDATED_ALERT_DOC = "BBBBBBBBBB";

    private static final String DEFAULT_ES_DOCUMENT_ID = "AAAAAAAAAA";
    private static final String UPDATED_ES_DOCUMENT_ID = "BBBBBBBBBB";

    private static final String DEFAULT_ES_INDEX_NAME = "AAAAAAAAAA";
    private static final String UPDATED_ES_INDEX_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/alarm-infos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/alarm-infos";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AlarmInfoRepository alarmInfoRepository;

    @Autowired
    private AlarmInfoSearchRepository alarmInfoSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlarmInfoMockMvc;

    private AlarmInfo alarmInfo;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlarmInfo createEntity(EntityManager em) {
        AlarmInfo alarmInfo = new AlarmInfo()
            .uuid(DEFAULT_UUID)
            .name(DEFAULT_NAME)
            .info(DEFAULT_INFO)
            .checked(DEFAULT_CHECKED)
            .createdTime(DEFAULT_CREATED_TIME)
            .createdBy(DEFAULT_CREATED_BY)
            .modifiedTime(DEFAULT_MODIFIED_TIME)
            .modifiedBy(DEFAULT_MODIFIED_BY)
            .alertTime(DEFAULT_ALERT_TIME)
            .alertDoc(DEFAULT_ALERT_DOC)
            .esDocumentId(DEFAULT_ES_DOCUMENT_ID)
            .esIndexName(DEFAULT_ES_INDEX_NAME);
        return alarmInfo;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlarmInfo createUpdatedEntity(EntityManager em) {
        AlarmInfo alarmInfo = new AlarmInfo()
            .uuid(UPDATED_UUID)
            .name(UPDATED_NAME)
            .info(UPDATED_INFO)
            .checked(UPDATED_CHECKED)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY)
            .alertTime(UPDATED_ALERT_TIME)
            .alertDoc(UPDATED_ALERT_DOC)
            .esDocumentId(UPDATED_ES_DOCUMENT_ID)
            .esIndexName(UPDATED_ES_INDEX_NAME);
        return alarmInfo;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        alarmInfoSearchRepository.deleteAll();
        assertThat(alarmInfoSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        alarmInfo = createEntity(em);
    }

    @Test
    @Transactional
    void createAlarmInfo() throws Exception {
        int databaseSizeBeforeCreate = alarmInfoRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        // Create the AlarmInfo
        restAlarmInfoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmInfo)))
            .andExpect(status().isCreated());

        // Validate the AlarmInfo in the database
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        AlarmInfo testAlarmInfo = alarmInfoList.get(alarmInfoList.size() - 1);
        assertThat(testAlarmInfo.getUuid()).isEqualTo(DEFAULT_UUID);
        assertThat(testAlarmInfo.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAlarmInfo.getInfo()).isEqualTo(DEFAULT_INFO);
        assertThat(testAlarmInfo.getChecked()).isEqualTo(DEFAULT_CHECKED);
        assertThat(testAlarmInfo.getCreatedTime()).isEqualTo(DEFAULT_CREATED_TIME);
        assertThat(testAlarmInfo.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testAlarmInfo.getModifiedTime()).isEqualTo(DEFAULT_MODIFIED_TIME);
        assertThat(testAlarmInfo.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
        assertThat(testAlarmInfo.getAlertTime()).isEqualTo(DEFAULT_ALERT_TIME);
        assertThat(testAlarmInfo.getAlertDoc()).isEqualTo(DEFAULT_ALERT_DOC);
        assertThat(testAlarmInfo.getEsDocumentId()).isEqualTo(DEFAULT_ES_DOCUMENT_ID);
        assertThat(testAlarmInfo.getEsIndexName()).isEqualTo(DEFAULT_ES_INDEX_NAME);
    }

    @Test
    @Transactional
    void createAlarmInfoWithExistingId() throws Exception {
        // Create the AlarmInfo with an existing ID
        alarmInfo.setId(1L);

        int databaseSizeBeforeCreate = alarmInfoRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlarmInfoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmInfo)))
            .andExpect(status().isBadRequest());

        // Validate the AlarmInfo in the database
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllAlarmInfos() throws Exception {
        // Initialize the database
        alarmInfoRepository.saveAndFlush(alarmInfo);

        // Get all the alarmInfoList
        restAlarmInfoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alarmInfo.getId().intValue())))
            .andExpect(jsonPath("$.[*].uuid").value(hasItem(DEFAULT_UUID)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].info").value(hasItem(DEFAULT_INFO)))
            .andExpect(jsonPath("$.[*].checked").value(hasItem(DEFAULT_CHECKED.booleanValue())))
            .andExpect(jsonPath("$.[*].createdTime").value(hasItem(sameInstant(DEFAULT_CREATED_TIME))))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedTime").value(hasItem(sameInstant(DEFAULT_MODIFIED_TIME))))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)))
            .andExpect(jsonPath("$.[*].alertTime").value(hasItem(sameInstant(DEFAULT_ALERT_TIME))))
            .andExpect(jsonPath("$.[*].alertDoc").value(hasItem(DEFAULT_ALERT_DOC)))
            .andExpect(jsonPath("$.[*].esDocumentId").value(hasItem(DEFAULT_ES_DOCUMENT_ID)))
            .andExpect(jsonPath("$.[*].esIndexName").value(hasItem(DEFAULT_ES_INDEX_NAME)));
    }

    @Test
    @Transactional
    void getAlarmInfo() throws Exception {
        // Initialize the database
        alarmInfoRepository.saveAndFlush(alarmInfo);

        // Get the alarmInfo
        restAlarmInfoMockMvc
            .perform(get(ENTITY_API_URL_ID, alarmInfo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(alarmInfo.getId().intValue()))
            .andExpect(jsonPath("$.uuid").value(DEFAULT_UUID))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.info").value(DEFAULT_INFO))
            .andExpect(jsonPath("$.checked").value(DEFAULT_CHECKED.booleanValue()))
            .andExpect(jsonPath("$.createdTime").value(sameInstant(DEFAULT_CREATED_TIME)))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.modifiedTime").value(sameInstant(DEFAULT_MODIFIED_TIME)))
            .andExpect(jsonPath("$.modifiedBy").value(DEFAULT_MODIFIED_BY))
            .andExpect(jsonPath("$.alertTime").value(sameInstant(DEFAULT_ALERT_TIME)))
            .andExpect(jsonPath("$.alertDoc").value(DEFAULT_ALERT_DOC))
            .andExpect(jsonPath("$.esDocumentId").value(DEFAULT_ES_DOCUMENT_ID))
            .andExpect(jsonPath("$.esIndexName").value(DEFAULT_ES_INDEX_NAME));
    }

    @Test
    @Transactional
    void getNonExistingAlarmInfo() throws Exception {
        // Get the alarmInfo
        restAlarmInfoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAlarmInfo() throws Exception {
        // Initialize the database
        alarmInfoRepository.saveAndFlush(alarmInfo);

        int databaseSizeBeforeUpdate = alarmInfoRepository.findAll().size();
        alarmInfoSearchRepository.save(alarmInfo);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());

        // Update the alarmInfo
        AlarmInfo updatedAlarmInfo = alarmInfoRepository.findById(alarmInfo.getId()).get();
        // Disconnect from session so that the updates on updatedAlarmInfo are not directly saved in db
        em.detach(updatedAlarmInfo);
        updatedAlarmInfo
            .uuid(UPDATED_UUID)
            .name(UPDATED_NAME)
            .info(UPDATED_INFO)
            .checked(UPDATED_CHECKED)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY)
            .alertTime(UPDATED_ALERT_TIME)
            .alertDoc(UPDATED_ALERT_DOC)
            .esDocumentId(UPDATED_ES_DOCUMENT_ID)
            .esIndexName(UPDATED_ES_INDEX_NAME);

        restAlarmInfoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAlarmInfo.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedAlarmInfo))
            )
            .andExpect(status().isOk());

        // Validate the AlarmInfo in the database
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeUpdate);
        AlarmInfo testAlarmInfo = alarmInfoList.get(alarmInfoList.size() - 1);
        assertThat(testAlarmInfo.getUuid()).isEqualTo(UPDATED_UUID);
        assertThat(testAlarmInfo.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlarmInfo.getInfo()).isEqualTo(UPDATED_INFO);
        assertThat(testAlarmInfo.getChecked()).isEqualTo(UPDATED_CHECKED);
        assertThat(testAlarmInfo.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testAlarmInfo.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testAlarmInfo.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testAlarmInfo.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        assertThat(testAlarmInfo.getAlertTime()).isEqualTo(UPDATED_ALERT_TIME);
        assertThat(testAlarmInfo.getAlertDoc()).isEqualTo(UPDATED_ALERT_DOC);
        assertThat(testAlarmInfo.getEsDocumentId()).isEqualTo(UPDATED_ES_DOCUMENT_ID);
        assertThat(testAlarmInfo.getEsIndexName()).isEqualTo(UPDATED_ES_INDEX_NAME);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<AlarmInfo> alarmInfoSearchList = IterableUtils.toList(alarmInfoSearchRepository.findAll());
                AlarmInfo testAlarmInfoSearch = alarmInfoSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testAlarmInfoSearch.getUuid()).isEqualTo(UPDATED_UUID);
                assertThat(testAlarmInfoSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testAlarmInfoSearch.getInfo()).isEqualTo(UPDATED_INFO);
                assertThat(testAlarmInfoSearch.getChecked()).isEqualTo(UPDATED_CHECKED);
                assertThat(testAlarmInfoSearch.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
                assertThat(testAlarmInfoSearch.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
                assertThat(testAlarmInfoSearch.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
                assertThat(testAlarmInfoSearch.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
                assertThat(testAlarmInfoSearch.getAlertTime()).isEqualTo(UPDATED_ALERT_TIME);
                assertThat(testAlarmInfoSearch.getAlertDoc()).isEqualTo(UPDATED_ALERT_DOC);
                assertThat(testAlarmInfoSearch.getEsDocumentId()).isEqualTo(UPDATED_ES_DOCUMENT_ID);
                assertThat(testAlarmInfoSearch.getEsIndexName()).isEqualTo(UPDATED_ES_INDEX_NAME);
            });
    }

    @Test
    @Transactional
    void putNonExistingAlarmInfo() throws Exception {
        int databaseSizeBeforeUpdate = alarmInfoRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        alarmInfo.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlarmInfoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alarmInfo.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(alarmInfo))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmInfo in the database
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchAlarmInfo() throws Exception {
        int databaseSizeBeforeUpdate = alarmInfoRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        alarmInfo.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmInfoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(alarmInfo))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmInfo in the database
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAlarmInfo() throws Exception {
        int databaseSizeBeforeUpdate = alarmInfoRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        alarmInfo.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmInfoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmInfo)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlarmInfo in the database
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateAlarmInfoWithPatch() throws Exception {
        // Initialize the database
        alarmInfoRepository.saveAndFlush(alarmInfo);

        int databaseSizeBeforeUpdate = alarmInfoRepository.findAll().size();

        // Update the alarmInfo using partial update
        AlarmInfo partialUpdatedAlarmInfo = new AlarmInfo();
        partialUpdatedAlarmInfo.setId(alarmInfo.getId());

        partialUpdatedAlarmInfo.info(UPDATED_INFO).checked(UPDATED_CHECKED).createdTime(UPDATED_CREATED_TIME);

        restAlarmInfoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlarmInfo.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlarmInfo))
            )
            .andExpect(status().isOk());

        // Validate the AlarmInfo in the database
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeUpdate);
        AlarmInfo testAlarmInfo = alarmInfoList.get(alarmInfoList.size() - 1);
        assertThat(testAlarmInfo.getUuid()).isEqualTo(DEFAULT_UUID);
        assertThat(testAlarmInfo.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAlarmInfo.getInfo()).isEqualTo(UPDATED_INFO);
        assertThat(testAlarmInfo.getChecked()).isEqualTo(UPDATED_CHECKED);
        assertThat(testAlarmInfo.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testAlarmInfo.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testAlarmInfo.getModifiedTime()).isEqualTo(DEFAULT_MODIFIED_TIME);
        assertThat(testAlarmInfo.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
        assertThat(testAlarmInfo.getAlertTime()).isEqualTo(DEFAULT_ALERT_TIME);
        assertThat(testAlarmInfo.getAlertDoc()).isEqualTo(DEFAULT_ALERT_DOC);
        assertThat(testAlarmInfo.getEsDocumentId()).isEqualTo(DEFAULT_ES_DOCUMENT_ID);
        assertThat(testAlarmInfo.getEsIndexName()).isEqualTo(DEFAULT_ES_INDEX_NAME);
    }

    @Test
    @Transactional
    void fullUpdateAlarmInfoWithPatch() throws Exception {
        // Initialize the database
        alarmInfoRepository.saveAndFlush(alarmInfo);

        int databaseSizeBeforeUpdate = alarmInfoRepository.findAll().size();

        // Update the alarmInfo using partial update
        AlarmInfo partialUpdatedAlarmInfo = new AlarmInfo();
        partialUpdatedAlarmInfo.setId(alarmInfo.getId());

        partialUpdatedAlarmInfo
            .uuid(UPDATED_UUID)
            .name(UPDATED_NAME)
            .info(UPDATED_INFO)
            .checked(UPDATED_CHECKED)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY)
            .alertTime(UPDATED_ALERT_TIME)
            .alertDoc(UPDATED_ALERT_DOC)
            .esDocumentId(UPDATED_ES_DOCUMENT_ID)
            .esIndexName(UPDATED_ES_INDEX_NAME);

        restAlarmInfoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlarmInfo.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlarmInfo))
            )
            .andExpect(status().isOk());

        // Validate the AlarmInfo in the database
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeUpdate);
        AlarmInfo testAlarmInfo = alarmInfoList.get(alarmInfoList.size() - 1);
        assertThat(testAlarmInfo.getUuid()).isEqualTo(UPDATED_UUID);
        assertThat(testAlarmInfo.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlarmInfo.getInfo()).isEqualTo(UPDATED_INFO);
        assertThat(testAlarmInfo.getChecked()).isEqualTo(UPDATED_CHECKED);
        assertThat(testAlarmInfo.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testAlarmInfo.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testAlarmInfo.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testAlarmInfo.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        assertThat(testAlarmInfo.getAlertTime()).isEqualTo(UPDATED_ALERT_TIME);
        assertThat(testAlarmInfo.getAlertDoc()).isEqualTo(UPDATED_ALERT_DOC);
        assertThat(testAlarmInfo.getEsDocumentId()).isEqualTo(UPDATED_ES_DOCUMENT_ID);
        assertThat(testAlarmInfo.getEsIndexName()).isEqualTo(UPDATED_ES_INDEX_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingAlarmInfo() throws Exception {
        int databaseSizeBeforeUpdate = alarmInfoRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        alarmInfo.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlarmInfoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, alarmInfo.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(alarmInfo))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmInfo in the database
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAlarmInfo() throws Exception {
        int databaseSizeBeforeUpdate = alarmInfoRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        alarmInfo.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmInfoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(alarmInfo))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmInfo in the database
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAlarmInfo() throws Exception {
        int databaseSizeBeforeUpdate = alarmInfoRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        alarmInfo.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmInfoMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(alarmInfo))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlarmInfo in the database
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteAlarmInfo() throws Exception {
        // Initialize the database
        alarmInfoRepository.saveAndFlush(alarmInfo);
        alarmInfoRepository.save(alarmInfo);
        alarmInfoSearchRepository.save(alarmInfo);

        int databaseSizeBeforeDelete = alarmInfoRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the alarmInfo
        restAlarmInfoMockMvc
            .perform(delete(ENTITY_API_URL_ID, alarmInfo.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AlarmInfo> alarmInfoList = alarmInfoRepository.findAll();
        assertThat(alarmInfoList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmInfoSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchAlarmInfo() throws Exception {
        // Initialize the database
        alarmInfo = alarmInfoRepository.saveAndFlush(alarmInfo);
        alarmInfoSearchRepository.save(alarmInfo);

        // Search the alarmInfo
        restAlarmInfoMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + alarmInfo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alarmInfo.getId().intValue())))
            .andExpect(jsonPath("$.[*].uuid").value(hasItem(DEFAULT_UUID)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].info").value(hasItem(DEFAULT_INFO)))
            .andExpect(jsonPath("$.[*].checked").value(hasItem(DEFAULT_CHECKED.booleanValue())))
            .andExpect(jsonPath("$.[*].createdTime").value(hasItem(sameInstant(DEFAULT_CREATED_TIME))))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedTime").value(hasItem(sameInstant(DEFAULT_MODIFIED_TIME))))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)))
            .andExpect(jsonPath("$.[*].alertTime").value(hasItem(sameInstant(DEFAULT_ALERT_TIME))))
            .andExpect(jsonPath("$.[*].alertDoc").value(hasItem(DEFAULT_ALERT_DOC)))
            .andExpect(jsonPath("$.[*].esDocumentId").value(hasItem(DEFAULT_ES_DOCUMENT_ID)))
            .andExpect(jsonPath("$.[*].esIndexName").value(hasItem(DEFAULT_ES_INDEX_NAME)));
    }
}
