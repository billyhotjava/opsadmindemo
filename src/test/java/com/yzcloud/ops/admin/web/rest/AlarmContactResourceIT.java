package com.yzcloud.ops.admin.web.rest;

import static com.yzcloud.ops.admin.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.yzcloud.ops.admin.IntegrationTest;
import com.yzcloud.ops.admin.domain.AlarmContact;
import com.yzcloud.ops.admin.repository.AlarmContactRepository;
import com.yzcloud.ops.admin.repository.search.AlarmContactSearchRepository;
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
 * Integration tests for the {@link AlarmContactResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AlarmContactResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_TYPE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_TYPE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_CONTACT_WAY = "AAAAAAAAAA";
    private static final String UPDATED_CONTACT_WAY = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final String DEFAULT_MODIFIED_TIME = "AAAAAAAAAA";
    private static final String UPDATED_MODIFIED_TIME = "BBBBBBBBBB";

    private static final String DEFAULT_MODIFIED_BY = "AAAAAAAAAA";
    private static final String UPDATED_MODIFIED_BY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/alarm-contacts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/alarm-contacts";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AlarmContactRepository alarmContactRepository;

    @Autowired
    private AlarmContactSearchRepository alarmContactSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlarmContactMockMvc;

    private AlarmContact alarmContact;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlarmContact createEntity(EntityManager em) {
        AlarmContact alarmContact = new AlarmContact()
            .title(DEFAULT_TITLE)
            .name(DEFAULT_NAME)
            .content(DEFAULT_CONTENT)
            .type(DEFAULT_TYPE)
            .contactWay(DEFAULT_CONTACT_WAY)
            .createdTime(DEFAULT_CREATED_TIME)
            .createdBy(DEFAULT_CREATED_BY)
            .modifiedTime(DEFAULT_MODIFIED_TIME)
            .modifiedBy(DEFAULT_MODIFIED_BY);
        return alarmContact;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlarmContact createUpdatedEntity(EntityManager em) {
        AlarmContact alarmContact = new AlarmContact()
            .title(UPDATED_TITLE)
            .name(UPDATED_NAME)
            .content(UPDATED_CONTENT)
            .type(UPDATED_TYPE)
            .contactWay(UPDATED_CONTACT_WAY)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY);
        return alarmContact;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        alarmContactSearchRepository.deleteAll();
        assertThat(alarmContactSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        alarmContact = createEntity(em);
    }

    @Test
    @Transactional
    void createAlarmContact() throws Exception {
        int databaseSizeBeforeCreate = alarmContactRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        // Create the AlarmContact
        restAlarmContactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmContact)))
            .andExpect(status().isCreated());

        // Validate the AlarmContact in the database
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        AlarmContact testAlarmContact = alarmContactList.get(alarmContactList.size() - 1);
        assertThat(testAlarmContact.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testAlarmContact.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAlarmContact.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testAlarmContact.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testAlarmContact.getContactWay()).isEqualTo(DEFAULT_CONTACT_WAY);
        assertThat(testAlarmContact.getCreatedTime()).isEqualTo(DEFAULT_CREATED_TIME);
        assertThat(testAlarmContact.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testAlarmContact.getModifiedTime()).isEqualTo(DEFAULT_MODIFIED_TIME);
        assertThat(testAlarmContact.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
    }

    @Test
    @Transactional
    void createAlarmContactWithExistingId() throws Exception {
        // Create the AlarmContact with an existing ID
        alarmContact.setId(1L);

        int databaseSizeBeforeCreate = alarmContactRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlarmContactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmContact)))
            .andExpect(status().isBadRequest());

        // Validate the AlarmContact in the database
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllAlarmContacts() throws Exception {
        // Initialize the database
        alarmContactRepository.saveAndFlush(alarmContact);

        // Get all the alarmContactList
        restAlarmContactMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alarmContact.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(sameInstant(DEFAULT_TYPE))))
            .andExpect(jsonPath("$.[*].contactWay").value(hasItem(DEFAULT_CONTACT_WAY)))
            .andExpect(jsonPath("$.[*].createdTime").value(hasItem(sameInstant(DEFAULT_CREATED_TIME))))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedTime").value(hasItem(DEFAULT_MODIFIED_TIME)))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)));
    }

    @Test
    @Transactional
    void getAlarmContact() throws Exception {
        // Initialize the database
        alarmContactRepository.saveAndFlush(alarmContact);

        // Get the alarmContact
        restAlarmContactMockMvc
            .perform(get(ENTITY_API_URL_ID, alarmContact.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(alarmContact.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT))
            .andExpect(jsonPath("$.type").value(sameInstant(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.contactWay").value(DEFAULT_CONTACT_WAY))
            .andExpect(jsonPath("$.createdTime").value(sameInstant(DEFAULT_CREATED_TIME)))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.modifiedTime").value(DEFAULT_MODIFIED_TIME))
            .andExpect(jsonPath("$.modifiedBy").value(DEFAULT_MODIFIED_BY));
    }

    @Test
    @Transactional
    void getNonExistingAlarmContact() throws Exception {
        // Get the alarmContact
        restAlarmContactMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAlarmContact() throws Exception {
        // Initialize the database
        alarmContactRepository.saveAndFlush(alarmContact);

        int databaseSizeBeforeUpdate = alarmContactRepository.findAll().size();
        alarmContactSearchRepository.save(alarmContact);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());

        // Update the alarmContact
        AlarmContact updatedAlarmContact = alarmContactRepository.findById(alarmContact.getId()).get();
        // Disconnect from session so that the updates on updatedAlarmContact are not directly saved in db
        em.detach(updatedAlarmContact);
        updatedAlarmContact
            .title(UPDATED_TITLE)
            .name(UPDATED_NAME)
            .content(UPDATED_CONTENT)
            .type(UPDATED_TYPE)
            .contactWay(UPDATED_CONTACT_WAY)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY);

        restAlarmContactMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAlarmContact.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedAlarmContact))
            )
            .andExpect(status().isOk());

        // Validate the AlarmContact in the database
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeUpdate);
        AlarmContact testAlarmContact = alarmContactList.get(alarmContactList.size() - 1);
        assertThat(testAlarmContact.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testAlarmContact.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlarmContact.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testAlarmContact.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testAlarmContact.getContactWay()).isEqualTo(UPDATED_CONTACT_WAY);
        assertThat(testAlarmContact.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testAlarmContact.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testAlarmContact.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testAlarmContact.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<AlarmContact> alarmContactSearchList = IterableUtils.toList(alarmContactSearchRepository.findAll());
                AlarmContact testAlarmContactSearch = alarmContactSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testAlarmContactSearch.getTitle()).isEqualTo(UPDATED_TITLE);
                assertThat(testAlarmContactSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testAlarmContactSearch.getContent()).isEqualTo(UPDATED_CONTENT);
                assertThat(testAlarmContactSearch.getType()).isEqualTo(UPDATED_TYPE);
                assertThat(testAlarmContactSearch.getContactWay()).isEqualTo(UPDATED_CONTACT_WAY);
                assertThat(testAlarmContactSearch.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
                assertThat(testAlarmContactSearch.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
                assertThat(testAlarmContactSearch.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
                assertThat(testAlarmContactSearch.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
            });
    }

    @Test
    @Transactional
    void putNonExistingAlarmContact() throws Exception {
        int databaseSizeBeforeUpdate = alarmContactRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        alarmContact.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlarmContactMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alarmContact.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(alarmContact))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmContact in the database
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchAlarmContact() throws Exception {
        int databaseSizeBeforeUpdate = alarmContactRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        alarmContact.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmContactMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(alarmContact))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmContact in the database
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAlarmContact() throws Exception {
        int databaseSizeBeforeUpdate = alarmContactRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        alarmContact.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmContactMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmContact)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlarmContact in the database
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateAlarmContactWithPatch() throws Exception {
        // Initialize the database
        alarmContactRepository.saveAndFlush(alarmContact);

        int databaseSizeBeforeUpdate = alarmContactRepository.findAll().size();

        // Update the alarmContact using partial update
        AlarmContact partialUpdatedAlarmContact = new AlarmContact();
        partialUpdatedAlarmContact.setId(alarmContact.getId());

        partialUpdatedAlarmContact
            .title(UPDATED_TITLE)
            .name(UPDATED_NAME)
            .content(UPDATED_CONTENT)
            .contactWay(UPDATED_CONTACT_WAY)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME);

        restAlarmContactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlarmContact.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlarmContact))
            )
            .andExpect(status().isOk());

        // Validate the AlarmContact in the database
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeUpdate);
        AlarmContact testAlarmContact = alarmContactList.get(alarmContactList.size() - 1);
        assertThat(testAlarmContact.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testAlarmContact.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlarmContact.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testAlarmContact.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testAlarmContact.getContactWay()).isEqualTo(UPDATED_CONTACT_WAY);
        assertThat(testAlarmContact.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testAlarmContact.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testAlarmContact.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testAlarmContact.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
    }

    @Test
    @Transactional
    void fullUpdateAlarmContactWithPatch() throws Exception {
        // Initialize the database
        alarmContactRepository.saveAndFlush(alarmContact);

        int databaseSizeBeforeUpdate = alarmContactRepository.findAll().size();

        // Update the alarmContact using partial update
        AlarmContact partialUpdatedAlarmContact = new AlarmContact();
        partialUpdatedAlarmContact.setId(alarmContact.getId());

        partialUpdatedAlarmContact
            .title(UPDATED_TITLE)
            .name(UPDATED_NAME)
            .content(UPDATED_CONTENT)
            .type(UPDATED_TYPE)
            .contactWay(UPDATED_CONTACT_WAY)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY);

        restAlarmContactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlarmContact.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlarmContact))
            )
            .andExpect(status().isOk());

        // Validate the AlarmContact in the database
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeUpdate);
        AlarmContact testAlarmContact = alarmContactList.get(alarmContactList.size() - 1);
        assertThat(testAlarmContact.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testAlarmContact.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlarmContact.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testAlarmContact.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testAlarmContact.getContactWay()).isEqualTo(UPDATED_CONTACT_WAY);
        assertThat(testAlarmContact.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testAlarmContact.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testAlarmContact.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testAlarmContact.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
    }

    @Test
    @Transactional
    void patchNonExistingAlarmContact() throws Exception {
        int databaseSizeBeforeUpdate = alarmContactRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        alarmContact.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlarmContactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, alarmContact.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(alarmContact))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmContact in the database
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAlarmContact() throws Exception {
        int databaseSizeBeforeUpdate = alarmContactRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        alarmContact.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmContactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(alarmContact))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmContact in the database
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAlarmContact() throws Exception {
        int databaseSizeBeforeUpdate = alarmContactRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        alarmContact.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmContactMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(alarmContact))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlarmContact in the database
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteAlarmContact() throws Exception {
        // Initialize the database
        alarmContactRepository.saveAndFlush(alarmContact);
        alarmContactRepository.save(alarmContact);
        alarmContactSearchRepository.save(alarmContact);

        int databaseSizeBeforeDelete = alarmContactRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the alarmContact
        restAlarmContactMockMvc
            .perform(delete(ENTITY_API_URL_ID, alarmContact.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AlarmContact> alarmContactList = alarmContactRepository.findAll();
        assertThat(alarmContactList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmContactSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchAlarmContact() throws Exception {
        // Initialize the database
        alarmContact = alarmContactRepository.saveAndFlush(alarmContact);
        alarmContactSearchRepository.save(alarmContact);

        // Search the alarmContact
        restAlarmContactMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + alarmContact.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alarmContact.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(sameInstant(DEFAULT_TYPE))))
            .andExpect(jsonPath("$.[*].contactWay").value(hasItem(DEFAULT_CONTACT_WAY)))
            .andExpect(jsonPath("$.[*].createdTime").value(hasItem(sameInstant(DEFAULT_CREATED_TIME))))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedTime").value(hasItem(DEFAULT_MODIFIED_TIME)))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)));
    }
}
