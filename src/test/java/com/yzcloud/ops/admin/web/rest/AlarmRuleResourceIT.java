package com.yzcloud.ops.admin.web.rest;

import static com.yzcloud.ops.admin.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.yzcloud.ops.admin.IntegrationTest;
import com.yzcloud.ops.admin.domain.AlarmRule;
import com.yzcloud.ops.admin.repository.AlarmRuleRepository;
import com.yzcloud.ops.admin.repository.search.AlarmRuleSearchRepository;
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
 * Integration tests for the {@link AlarmRuleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AlarmRuleResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_ALARM_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_ALARM_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_CONF = "AAAAAAAAAA";
    private static final String UPDATED_CONF = "BBBBBBBBBB";

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_MODIFIED_BY = "AAAAAAAAAA";
    private static final String UPDATED_MODIFIED_BY = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_MODIFIED_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_MODIFIED_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/alarm-rules";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/alarm-rules";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AlarmRuleRepository alarmRuleRepository;

    @Autowired
    private AlarmRuleSearchRepository alarmRuleSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlarmRuleMockMvc;

    private AlarmRule alarmRule;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlarmRule createEntity(EntityManager em) {
        AlarmRule alarmRule = new AlarmRule()
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .alarmType(DEFAULT_ALARM_TYPE)
            .conf(DEFAULT_CONF)
            .createdBy(DEFAULT_CREATED_BY)
            .createTime(DEFAULT_CREATE_TIME)
            .modifiedBy(DEFAULT_MODIFIED_BY)
            .modifiedTime(DEFAULT_MODIFIED_TIME)
            .status(DEFAULT_STATUS);
        return alarmRule;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlarmRule createUpdatedEntity(EntityManager em) {
        AlarmRule alarmRule = new AlarmRule()
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .alarmType(UPDATED_ALARM_TYPE)
            .conf(UPDATED_CONF)
            .createdBy(UPDATED_CREATED_BY)
            .createTime(UPDATED_CREATE_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .status(UPDATED_STATUS);
        return alarmRule;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        alarmRuleSearchRepository.deleteAll();
        assertThat(alarmRuleSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        alarmRule = createEntity(em);
    }

    @Test
    @Transactional
    void createAlarmRule() throws Exception {
        int databaseSizeBeforeCreate = alarmRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        // Create the AlarmRule
        restAlarmRuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmRule)))
            .andExpect(status().isCreated());

        // Validate the AlarmRule in the database
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        AlarmRule testAlarmRule = alarmRuleList.get(alarmRuleList.size() - 1);
        assertThat(testAlarmRule.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAlarmRule.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAlarmRule.getAlarmType()).isEqualTo(DEFAULT_ALARM_TYPE);
        assertThat(testAlarmRule.getConf()).isEqualTo(DEFAULT_CONF);
        assertThat(testAlarmRule.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testAlarmRule.getCreateTime()).isEqualTo(DEFAULT_CREATE_TIME);
        assertThat(testAlarmRule.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
        assertThat(testAlarmRule.getModifiedTime()).isEqualTo(DEFAULT_MODIFIED_TIME);
        assertThat(testAlarmRule.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    void createAlarmRuleWithExistingId() throws Exception {
        // Create the AlarmRule with an existing ID
        alarmRule.setId(1L);

        int databaseSizeBeforeCreate = alarmRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlarmRuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmRule)))
            .andExpect(status().isBadRequest());

        // Validate the AlarmRule in the database
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllAlarmRules() throws Exception {
        // Initialize the database
        alarmRuleRepository.saveAndFlush(alarmRule);

        // Get all the alarmRuleList
        restAlarmRuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alarmRule.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].alarmType").value(hasItem(DEFAULT_ALARM_TYPE)))
            .andExpect(jsonPath("$.[*].conf").value(hasItem(DEFAULT_CONF)))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)))
            .andExpect(jsonPath("$.[*].modifiedTime").value(hasItem(sameInstant(DEFAULT_MODIFIED_TIME))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }

    @Test
    @Transactional
    void getAlarmRule() throws Exception {
        // Initialize the database
        alarmRuleRepository.saveAndFlush(alarmRule);

        // Get the alarmRule
        restAlarmRuleMockMvc
            .perform(get(ENTITY_API_URL_ID, alarmRule.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(alarmRule.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.alarmType").value(DEFAULT_ALARM_TYPE))
            .andExpect(jsonPath("$.conf").value(DEFAULT_CONF))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.createTime").value(sameInstant(DEFAULT_CREATE_TIME)))
            .andExpect(jsonPath("$.modifiedBy").value(DEFAULT_MODIFIED_BY))
            .andExpect(jsonPath("$.modifiedTime").value(sameInstant(DEFAULT_MODIFIED_TIME)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS));
    }

    @Test
    @Transactional
    void getNonExistingAlarmRule() throws Exception {
        // Get the alarmRule
        restAlarmRuleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAlarmRule() throws Exception {
        // Initialize the database
        alarmRuleRepository.saveAndFlush(alarmRule);

        int databaseSizeBeforeUpdate = alarmRuleRepository.findAll().size();
        alarmRuleSearchRepository.save(alarmRule);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());

        // Update the alarmRule
        AlarmRule updatedAlarmRule = alarmRuleRepository.findById(alarmRule.getId()).get();
        // Disconnect from session so that the updates on updatedAlarmRule are not directly saved in db
        em.detach(updatedAlarmRule);
        updatedAlarmRule
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .alarmType(UPDATED_ALARM_TYPE)
            .conf(UPDATED_CONF)
            .createdBy(UPDATED_CREATED_BY)
            .createTime(UPDATED_CREATE_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .status(UPDATED_STATUS);

        restAlarmRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAlarmRule.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedAlarmRule))
            )
            .andExpect(status().isOk());

        // Validate the AlarmRule in the database
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeUpdate);
        AlarmRule testAlarmRule = alarmRuleList.get(alarmRuleList.size() - 1);
        assertThat(testAlarmRule.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlarmRule.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAlarmRule.getAlarmType()).isEqualTo(UPDATED_ALARM_TYPE);
        assertThat(testAlarmRule.getConf()).isEqualTo(UPDATED_CONF);
        assertThat(testAlarmRule.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testAlarmRule.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
        assertThat(testAlarmRule.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        assertThat(testAlarmRule.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testAlarmRule.getStatus()).isEqualTo(UPDATED_STATUS);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<AlarmRule> alarmRuleSearchList = IterableUtils.toList(alarmRuleSearchRepository.findAll());
                AlarmRule testAlarmRuleSearch = alarmRuleSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testAlarmRuleSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testAlarmRuleSearch.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
                assertThat(testAlarmRuleSearch.getAlarmType()).isEqualTo(UPDATED_ALARM_TYPE);
                assertThat(testAlarmRuleSearch.getConf()).isEqualTo(UPDATED_CONF);
                assertThat(testAlarmRuleSearch.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
                assertThat(testAlarmRuleSearch.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
                assertThat(testAlarmRuleSearch.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
                assertThat(testAlarmRuleSearch.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
                assertThat(testAlarmRuleSearch.getStatus()).isEqualTo(UPDATED_STATUS);
            });
    }

    @Test
    @Transactional
    void putNonExistingAlarmRule() throws Exception {
        int databaseSizeBeforeUpdate = alarmRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        alarmRule.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlarmRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alarmRule.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(alarmRule))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmRule in the database
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchAlarmRule() throws Exception {
        int databaseSizeBeforeUpdate = alarmRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        alarmRule.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(alarmRule))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmRule in the database
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAlarmRule() throws Exception {
        int databaseSizeBeforeUpdate = alarmRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        alarmRule.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmRuleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmRule)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlarmRule in the database
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateAlarmRuleWithPatch() throws Exception {
        // Initialize the database
        alarmRuleRepository.saveAndFlush(alarmRule);

        int databaseSizeBeforeUpdate = alarmRuleRepository.findAll().size();

        // Update the alarmRule using partial update
        AlarmRule partialUpdatedAlarmRule = new AlarmRule();
        partialUpdatedAlarmRule.setId(alarmRule.getId());

        partialUpdatedAlarmRule
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .alarmType(UPDATED_ALARM_TYPE)
            .createdBy(UPDATED_CREATED_BY)
            .createTime(UPDATED_CREATE_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY)
            .status(UPDATED_STATUS);

        restAlarmRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlarmRule.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlarmRule))
            )
            .andExpect(status().isOk());

        // Validate the AlarmRule in the database
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeUpdate);
        AlarmRule testAlarmRule = alarmRuleList.get(alarmRuleList.size() - 1);
        assertThat(testAlarmRule.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlarmRule.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAlarmRule.getAlarmType()).isEqualTo(UPDATED_ALARM_TYPE);
        assertThat(testAlarmRule.getConf()).isEqualTo(DEFAULT_CONF);
        assertThat(testAlarmRule.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testAlarmRule.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
        assertThat(testAlarmRule.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        assertThat(testAlarmRule.getModifiedTime()).isEqualTo(DEFAULT_MODIFIED_TIME);
        assertThat(testAlarmRule.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    void fullUpdateAlarmRuleWithPatch() throws Exception {
        // Initialize the database
        alarmRuleRepository.saveAndFlush(alarmRule);

        int databaseSizeBeforeUpdate = alarmRuleRepository.findAll().size();

        // Update the alarmRule using partial update
        AlarmRule partialUpdatedAlarmRule = new AlarmRule();
        partialUpdatedAlarmRule.setId(alarmRule.getId());

        partialUpdatedAlarmRule
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .alarmType(UPDATED_ALARM_TYPE)
            .conf(UPDATED_CONF)
            .createdBy(UPDATED_CREATED_BY)
            .createTime(UPDATED_CREATE_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .status(UPDATED_STATUS);

        restAlarmRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlarmRule.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlarmRule))
            )
            .andExpect(status().isOk());

        // Validate the AlarmRule in the database
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeUpdate);
        AlarmRule testAlarmRule = alarmRuleList.get(alarmRuleList.size() - 1);
        assertThat(testAlarmRule.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlarmRule.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAlarmRule.getAlarmType()).isEqualTo(UPDATED_ALARM_TYPE);
        assertThat(testAlarmRule.getConf()).isEqualTo(UPDATED_CONF);
        assertThat(testAlarmRule.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testAlarmRule.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
        assertThat(testAlarmRule.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        assertThat(testAlarmRule.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testAlarmRule.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    void patchNonExistingAlarmRule() throws Exception {
        int databaseSizeBeforeUpdate = alarmRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        alarmRule.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlarmRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, alarmRule.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(alarmRule))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmRule in the database
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAlarmRule() throws Exception {
        int databaseSizeBeforeUpdate = alarmRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        alarmRule.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(alarmRule))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmRule in the database
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAlarmRule() throws Exception {
        int databaseSizeBeforeUpdate = alarmRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        alarmRule.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmRuleMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(alarmRule))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlarmRule in the database
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteAlarmRule() throws Exception {
        // Initialize the database
        alarmRuleRepository.saveAndFlush(alarmRule);
        alarmRuleRepository.save(alarmRule);
        alarmRuleSearchRepository.save(alarmRule);

        int databaseSizeBeforeDelete = alarmRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the alarmRule
        restAlarmRuleMockMvc
            .perform(delete(ENTITY_API_URL_ID, alarmRule.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AlarmRule> alarmRuleList = alarmRuleRepository.findAll();
        assertThat(alarmRuleList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchAlarmRule() throws Exception {
        // Initialize the database
        alarmRule = alarmRuleRepository.saveAndFlush(alarmRule);
        alarmRuleSearchRepository.save(alarmRule);

        // Search the alarmRule
        restAlarmRuleMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + alarmRule.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alarmRule.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].alarmType").value(hasItem(DEFAULT_ALARM_TYPE)))
            .andExpect(jsonPath("$.[*].conf").value(hasItem(DEFAULT_CONF)))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)))
            .andExpect(jsonPath("$.[*].modifiedTime").value(hasItem(sameInstant(DEFAULT_MODIFIED_TIME))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }
}
