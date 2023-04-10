package com.yzcloud.ops.admin.web.rest;

import static com.yzcloud.ops.admin.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.yzcloud.ops.admin.IntegrationTest;
import com.yzcloud.ops.admin.domain.EventRule;
import com.yzcloud.ops.admin.repository.EventRuleRepository;
import com.yzcloud.ops.admin.repository.search.EventRuleSearchRepository;
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
 * Integration tests for the {@link EventRuleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EventRuleResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ALIAS = "AAAAAAAAAA";
    private static final String UPDATED_ALIAS = "BBBBBBBBBB";

    private static final String DEFAULT_EVENT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_EVENT_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_EVENT_SAMPLE = "AAAAAAAAAA";
    private static final String UPDATED_EVENT_SAMPLE = "BBBBBBBBBB";

    private static final String DEFAULT_EVENT_RULE = "AAAAAAAAAA";
    private static final String UPDATED_EVENT_RULE = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_MODIFIED_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_MODIFIED_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_MODIFIED_BY = "AAAAAAAAAA";
    private static final String UPDATED_MODIFIED_BY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/event-rules";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/event-rules";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private EventRuleRepository eventRuleRepository;

    @Autowired
    private EventRuleSearchRepository eventRuleSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEventRuleMockMvc;

    private EventRule eventRule;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventRule createEntity(EntityManager em) {
        EventRule eventRule = new EventRule()
            .name(DEFAULT_NAME)
            .alias(DEFAULT_ALIAS)
            .eventType(DEFAULT_EVENT_TYPE)
            .description(DEFAULT_DESCRIPTION)
            .eventSample(DEFAULT_EVENT_SAMPLE)
            .eventRule(DEFAULT_EVENT_RULE)
            .createdTime(DEFAULT_CREATED_TIME)
            .createdBy(DEFAULT_CREATED_BY)
            .modifiedTime(DEFAULT_MODIFIED_TIME)
            .modifiedBy(DEFAULT_MODIFIED_BY);
        return eventRule;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventRule createUpdatedEntity(EntityManager em) {
        EventRule eventRule = new EventRule()
            .name(UPDATED_NAME)
            .alias(UPDATED_ALIAS)
            .eventType(UPDATED_EVENT_TYPE)
            .description(UPDATED_DESCRIPTION)
            .eventSample(UPDATED_EVENT_SAMPLE)
            .eventRule(UPDATED_EVENT_RULE)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY);
        return eventRule;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        eventRuleSearchRepository.deleteAll();
        assertThat(eventRuleSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        eventRule = createEntity(em);
    }

    @Test
    @Transactional
    void createEventRule() throws Exception {
        int databaseSizeBeforeCreate = eventRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        // Create the EventRule
        restEventRuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(eventRule)))
            .andExpect(status().isCreated());

        // Validate the EventRule in the database
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        EventRule testEventRule = eventRuleList.get(eventRuleList.size() - 1);
        assertThat(testEventRule.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testEventRule.getAlias()).isEqualTo(DEFAULT_ALIAS);
        assertThat(testEventRule.getEventType()).isEqualTo(DEFAULT_EVENT_TYPE);
        assertThat(testEventRule.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testEventRule.getEventSample()).isEqualTo(DEFAULT_EVENT_SAMPLE);
        assertThat(testEventRule.getEventRule()).isEqualTo(DEFAULT_EVENT_RULE);
        assertThat(testEventRule.getCreatedTime()).isEqualTo(DEFAULT_CREATED_TIME);
        assertThat(testEventRule.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testEventRule.getModifiedTime()).isEqualTo(DEFAULT_MODIFIED_TIME);
        assertThat(testEventRule.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
    }

    @Test
    @Transactional
    void createEventRuleWithExistingId() throws Exception {
        // Create the EventRule with an existing ID
        eventRule.setId(1L);

        int databaseSizeBeforeCreate = eventRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventRuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(eventRule)))
            .andExpect(status().isBadRequest());

        // Validate the EventRule in the database
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllEventRules() throws Exception {
        // Initialize the database
        eventRuleRepository.saveAndFlush(eventRule);

        // Get all the eventRuleList
        restEventRuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventRule.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].alias").value(hasItem(DEFAULT_ALIAS)))
            .andExpect(jsonPath("$.[*].eventType").value(hasItem(DEFAULT_EVENT_TYPE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].eventSample").value(hasItem(DEFAULT_EVENT_SAMPLE)))
            .andExpect(jsonPath("$.[*].eventRule").value(hasItem(DEFAULT_EVENT_RULE)))
            .andExpect(jsonPath("$.[*].createdTime").value(hasItem(sameInstant(DEFAULT_CREATED_TIME))))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedTime").value(hasItem(sameInstant(DEFAULT_MODIFIED_TIME))))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)));
    }

    @Test
    @Transactional
    void getEventRule() throws Exception {
        // Initialize the database
        eventRuleRepository.saveAndFlush(eventRule);

        // Get the eventRule
        restEventRuleMockMvc
            .perform(get(ENTITY_API_URL_ID, eventRule.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(eventRule.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.alias").value(DEFAULT_ALIAS))
            .andExpect(jsonPath("$.eventType").value(DEFAULT_EVENT_TYPE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.eventSample").value(DEFAULT_EVENT_SAMPLE))
            .andExpect(jsonPath("$.eventRule").value(DEFAULT_EVENT_RULE))
            .andExpect(jsonPath("$.createdTime").value(sameInstant(DEFAULT_CREATED_TIME)))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.modifiedTime").value(sameInstant(DEFAULT_MODIFIED_TIME)))
            .andExpect(jsonPath("$.modifiedBy").value(DEFAULT_MODIFIED_BY));
    }

    @Test
    @Transactional
    void getNonExistingEventRule() throws Exception {
        // Get the eventRule
        restEventRuleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEventRule() throws Exception {
        // Initialize the database
        eventRuleRepository.saveAndFlush(eventRule);

        int databaseSizeBeforeUpdate = eventRuleRepository.findAll().size();
        eventRuleSearchRepository.save(eventRule);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());

        // Update the eventRule
        EventRule updatedEventRule = eventRuleRepository.findById(eventRule.getId()).get();
        // Disconnect from session so that the updates on updatedEventRule are not directly saved in db
        em.detach(updatedEventRule);
        updatedEventRule
            .name(UPDATED_NAME)
            .alias(UPDATED_ALIAS)
            .eventType(UPDATED_EVENT_TYPE)
            .description(UPDATED_DESCRIPTION)
            .eventSample(UPDATED_EVENT_SAMPLE)
            .eventRule(UPDATED_EVENT_RULE)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY);

        restEventRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedEventRule.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedEventRule))
            )
            .andExpect(status().isOk());

        // Validate the EventRule in the database
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeUpdate);
        EventRule testEventRule = eventRuleList.get(eventRuleList.size() - 1);
        assertThat(testEventRule.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testEventRule.getAlias()).isEqualTo(UPDATED_ALIAS);
        assertThat(testEventRule.getEventType()).isEqualTo(UPDATED_EVENT_TYPE);
        assertThat(testEventRule.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testEventRule.getEventSample()).isEqualTo(UPDATED_EVENT_SAMPLE);
        assertThat(testEventRule.getEventRule()).isEqualTo(UPDATED_EVENT_RULE);
        assertThat(testEventRule.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testEventRule.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testEventRule.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testEventRule.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<EventRule> eventRuleSearchList = IterableUtils.toList(eventRuleSearchRepository.findAll());
                EventRule testEventRuleSearch = eventRuleSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testEventRuleSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testEventRuleSearch.getAlias()).isEqualTo(UPDATED_ALIAS);
                assertThat(testEventRuleSearch.getEventType()).isEqualTo(UPDATED_EVENT_TYPE);
                assertThat(testEventRuleSearch.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
                assertThat(testEventRuleSearch.getEventSample()).isEqualTo(UPDATED_EVENT_SAMPLE);
                assertThat(testEventRuleSearch.getEventRule()).isEqualTo(UPDATED_EVENT_RULE);
                assertThat(testEventRuleSearch.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
                assertThat(testEventRuleSearch.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
                assertThat(testEventRuleSearch.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
                assertThat(testEventRuleSearch.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
            });
    }

    @Test
    @Transactional
    void putNonExistingEventRule() throws Exception {
        int databaseSizeBeforeUpdate = eventRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        eventRule.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventRule.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(eventRule))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventRule in the database
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchEventRule() throws Exception {
        int databaseSizeBeforeUpdate = eventRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        eventRule.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(eventRule))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventRule in the database
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEventRule() throws Exception {
        int databaseSizeBeforeUpdate = eventRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        eventRule.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventRuleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(eventRule)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the EventRule in the database
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateEventRuleWithPatch() throws Exception {
        // Initialize the database
        eventRuleRepository.saveAndFlush(eventRule);

        int databaseSizeBeforeUpdate = eventRuleRepository.findAll().size();

        // Update the eventRule using partial update
        EventRule partialUpdatedEventRule = new EventRule();
        partialUpdatedEventRule.setId(eventRule.getId());

        partialUpdatedEventRule
            .name(UPDATED_NAME)
            .eventType(UPDATED_EVENT_TYPE)
            .createdTime(UPDATED_CREATED_TIME)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY);

        restEventRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventRule.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedEventRule))
            )
            .andExpect(status().isOk());

        // Validate the EventRule in the database
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeUpdate);
        EventRule testEventRule = eventRuleList.get(eventRuleList.size() - 1);
        assertThat(testEventRule.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testEventRule.getAlias()).isEqualTo(DEFAULT_ALIAS);
        assertThat(testEventRule.getEventType()).isEqualTo(UPDATED_EVENT_TYPE);
        assertThat(testEventRule.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testEventRule.getEventSample()).isEqualTo(DEFAULT_EVENT_SAMPLE);
        assertThat(testEventRule.getEventRule()).isEqualTo(DEFAULT_EVENT_RULE);
        assertThat(testEventRule.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testEventRule.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testEventRule.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testEventRule.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
    }

    @Test
    @Transactional
    void fullUpdateEventRuleWithPatch() throws Exception {
        // Initialize the database
        eventRuleRepository.saveAndFlush(eventRule);

        int databaseSizeBeforeUpdate = eventRuleRepository.findAll().size();

        // Update the eventRule using partial update
        EventRule partialUpdatedEventRule = new EventRule();
        partialUpdatedEventRule.setId(eventRule.getId());

        partialUpdatedEventRule
            .name(UPDATED_NAME)
            .alias(UPDATED_ALIAS)
            .eventType(UPDATED_EVENT_TYPE)
            .description(UPDATED_DESCRIPTION)
            .eventSample(UPDATED_EVENT_SAMPLE)
            .eventRule(UPDATED_EVENT_RULE)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY);

        restEventRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventRule.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedEventRule))
            )
            .andExpect(status().isOk());

        // Validate the EventRule in the database
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeUpdate);
        EventRule testEventRule = eventRuleList.get(eventRuleList.size() - 1);
        assertThat(testEventRule.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testEventRule.getAlias()).isEqualTo(UPDATED_ALIAS);
        assertThat(testEventRule.getEventType()).isEqualTo(UPDATED_EVENT_TYPE);
        assertThat(testEventRule.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testEventRule.getEventSample()).isEqualTo(UPDATED_EVENT_SAMPLE);
        assertThat(testEventRule.getEventRule()).isEqualTo(UPDATED_EVENT_RULE);
        assertThat(testEventRule.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testEventRule.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testEventRule.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testEventRule.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
    }

    @Test
    @Transactional
    void patchNonExistingEventRule() throws Exception {
        int databaseSizeBeforeUpdate = eventRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        eventRule.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, eventRule.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(eventRule))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventRule in the database
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEventRule() throws Exception {
        int databaseSizeBeforeUpdate = eventRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        eventRule.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(eventRule))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventRule in the database
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEventRule() throws Exception {
        int databaseSizeBeforeUpdate = eventRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        eventRule.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventRuleMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(eventRule))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the EventRule in the database
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteEventRule() throws Exception {
        // Initialize the database
        eventRuleRepository.saveAndFlush(eventRule);
        eventRuleRepository.save(eventRule);
        eventRuleSearchRepository.save(eventRule);

        int databaseSizeBeforeDelete = eventRuleRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the eventRule
        restEventRuleMockMvc
            .perform(delete(ENTITY_API_URL_ID, eventRule.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<EventRule> eventRuleList = eventRuleRepository.findAll();
        assertThat(eventRuleList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventRuleSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchEventRule() throws Exception {
        // Initialize the database
        eventRule = eventRuleRepository.saveAndFlush(eventRule);
        eventRuleSearchRepository.save(eventRule);

        // Search the eventRule
        restEventRuleMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + eventRule.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventRule.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].alias").value(hasItem(DEFAULT_ALIAS)))
            .andExpect(jsonPath("$.[*].eventType").value(hasItem(DEFAULT_EVENT_TYPE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].eventSample").value(hasItem(DEFAULT_EVENT_SAMPLE)))
            .andExpect(jsonPath("$.[*].eventRule").value(hasItem(DEFAULT_EVENT_RULE)))
            .andExpect(jsonPath("$.[*].createdTime").value(hasItem(sameInstant(DEFAULT_CREATED_TIME))))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedTime").value(hasItem(sameInstant(DEFAULT_MODIFIED_TIME))))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)));
    }
}
