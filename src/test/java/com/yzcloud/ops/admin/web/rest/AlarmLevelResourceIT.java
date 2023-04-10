package com.yzcloud.ops.admin.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.yzcloud.ops.admin.IntegrationTest;
import com.yzcloud.ops.admin.domain.AlarmLevel;
import com.yzcloud.ops.admin.repository.AlarmLevelRepository;
import com.yzcloud.ops.admin.repository.search.AlarmLevelSearchRepository;
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
 * Integration tests for the {@link AlarmLevelResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AlarmLevelResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_COLOR = "AAAAAAAAAA";
    private static final String UPDATED_COLOR = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/alarm-levels";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/alarm-levels";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AlarmLevelRepository alarmLevelRepository;

    @Autowired
    private AlarmLevelSearchRepository alarmLevelSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlarmLevelMockMvc;

    private AlarmLevel alarmLevel;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlarmLevel createEntity(EntityManager em) {
        AlarmLevel alarmLevel = new AlarmLevel().name(DEFAULT_NAME).color(DEFAULT_COLOR).description(DEFAULT_DESCRIPTION);
        return alarmLevel;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlarmLevel createUpdatedEntity(EntityManager em) {
        AlarmLevel alarmLevel = new AlarmLevel().name(UPDATED_NAME).color(UPDATED_COLOR).description(UPDATED_DESCRIPTION);
        return alarmLevel;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        alarmLevelSearchRepository.deleteAll();
        assertThat(alarmLevelSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        alarmLevel = createEntity(em);
    }

    @Test
    @Transactional
    void createAlarmLevel() throws Exception {
        int databaseSizeBeforeCreate = alarmLevelRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        // Create the AlarmLevel
        restAlarmLevelMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmLevel)))
            .andExpect(status().isCreated());

        // Validate the AlarmLevel in the database
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        AlarmLevel testAlarmLevel = alarmLevelList.get(alarmLevelList.size() - 1);
        assertThat(testAlarmLevel.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAlarmLevel.getColor()).isEqualTo(DEFAULT_COLOR);
        assertThat(testAlarmLevel.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void createAlarmLevelWithExistingId() throws Exception {
        // Create the AlarmLevel with an existing ID
        alarmLevel.setId(1L);

        int databaseSizeBeforeCreate = alarmLevelRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlarmLevelMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmLevel)))
            .andExpect(status().isBadRequest());

        // Validate the AlarmLevel in the database
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllAlarmLevels() throws Exception {
        // Initialize the database
        alarmLevelRepository.saveAndFlush(alarmLevel);

        // Get all the alarmLevelList
        restAlarmLevelMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alarmLevel.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].color").value(hasItem(DEFAULT_COLOR)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getAlarmLevel() throws Exception {
        // Initialize the database
        alarmLevelRepository.saveAndFlush(alarmLevel);

        // Get the alarmLevel
        restAlarmLevelMockMvc
            .perform(get(ENTITY_API_URL_ID, alarmLevel.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(alarmLevel.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.color").value(DEFAULT_COLOR))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingAlarmLevel() throws Exception {
        // Get the alarmLevel
        restAlarmLevelMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAlarmLevel() throws Exception {
        // Initialize the database
        alarmLevelRepository.saveAndFlush(alarmLevel);

        int databaseSizeBeforeUpdate = alarmLevelRepository.findAll().size();
        alarmLevelSearchRepository.save(alarmLevel);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());

        // Update the alarmLevel
        AlarmLevel updatedAlarmLevel = alarmLevelRepository.findById(alarmLevel.getId()).get();
        // Disconnect from session so that the updates on updatedAlarmLevel are not directly saved in db
        em.detach(updatedAlarmLevel);
        updatedAlarmLevel.name(UPDATED_NAME).color(UPDATED_COLOR).description(UPDATED_DESCRIPTION);

        restAlarmLevelMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAlarmLevel.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedAlarmLevel))
            )
            .andExpect(status().isOk());

        // Validate the AlarmLevel in the database
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeUpdate);
        AlarmLevel testAlarmLevel = alarmLevelList.get(alarmLevelList.size() - 1);
        assertThat(testAlarmLevel.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlarmLevel.getColor()).isEqualTo(UPDATED_COLOR);
        assertThat(testAlarmLevel.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<AlarmLevel> alarmLevelSearchList = IterableUtils.toList(alarmLevelSearchRepository.findAll());
                AlarmLevel testAlarmLevelSearch = alarmLevelSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testAlarmLevelSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testAlarmLevelSearch.getColor()).isEqualTo(UPDATED_COLOR);
                assertThat(testAlarmLevelSearch.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
            });
    }

    @Test
    @Transactional
    void putNonExistingAlarmLevel() throws Exception {
        int databaseSizeBeforeUpdate = alarmLevelRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        alarmLevel.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlarmLevelMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alarmLevel.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(alarmLevel))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmLevel in the database
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchAlarmLevel() throws Exception {
        int databaseSizeBeforeUpdate = alarmLevelRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        alarmLevel.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmLevelMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(alarmLevel))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmLevel in the database
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAlarmLevel() throws Exception {
        int databaseSizeBeforeUpdate = alarmLevelRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        alarmLevel.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmLevelMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alarmLevel)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlarmLevel in the database
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateAlarmLevelWithPatch() throws Exception {
        // Initialize the database
        alarmLevelRepository.saveAndFlush(alarmLevel);

        int databaseSizeBeforeUpdate = alarmLevelRepository.findAll().size();

        // Update the alarmLevel using partial update
        AlarmLevel partialUpdatedAlarmLevel = new AlarmLevel();
        partialUpdatedAlarmLevel.setId(alarmLevel.getId());

        partialUpdatedAlarmLevel.name(UPDATED_NAME);

        restAlarmLevelMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlarmLevel.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlarmLevel))
            )
            .andExpect(status().isOk());

        // Validate the AlarmLevel in the database
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeUpdate);
        AlarmLevel testAlarmLevel = alarmLevelList.get(alarmLevelList.size() - 1);
        assertThat(testAlarmLevel.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlarmLevel.getColor()).isEqualTo(DEFAULT_COLOR);
        assertThat(testAlarmLevel.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void fullUpdateAlarmLevelWithPatch() throws Exception {
        // Initialize the database
        alarmLevelRepository.saveAndFlush(alarmLevel);

        int databaseSizeBeforeUpdate = alarmLevelRepository.findAll().size();

        // Update the alarmLevel using partial update
        AlarmLevel partialUpdatedAlarmLevel = new AlarmLevel();
        partialUpdatedAlarmLevel.setId(alarmLevel.getId());

        partialUpdatedAlarmLevel.name(UPDATED_NAME).color(UPDATED_COLOR).description(UPDATED_DESCRIPTION);

        restAlarmLevelMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlarmLevel.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlarmLevel))
            )
            .andExpect(status().isOk());

        // Validate the AlarmLevel in the database
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeUpdate);
        AlarmLevel testAlarmLevel = alarmLevelList.get(alarmLevelList.size() - 1);
        assertThat(testAlarmLevel.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlarmLevel.getColor()).isEqualTo(UPDATED_COLOR);
        assertThat(testAlarmLevel.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void patchNonExistingAlarmLevel() throws Exception {
        int databaseSizeBeforeUpdate = alarmLevelRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        alarmLevel.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlarmLevelMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, alarmLevel.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(alarmLevel))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmLevel in the database
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAlarmLevel() throws Exception {
        int databaseSizeBeforeUpdate = alarmLevelRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        alarmLevel.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmLevelMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(alarmLevel))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlarmLevel in the database
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAlarmLevel() throws Exception {
        int databaseSizeBeforeUpdate = alarmLevelRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        alarmLevel.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmLevelMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(alarmLevel))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlarmLevel in the database
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteAlarmLevel() throws Exception {
        // Initialize the database
        alarmLevelRepository.saveAndFlush(alarmLevel);
        alarmLevelRepository.save(alarmLevel);
        alarmLevelSearchRepository.save(alarmLevel);

        int databaseSizeBeforeDelete = alarmLevelRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the alarmLevel
        restAlarmLevelMockMvc
            .perform(delete(ENTITY_API_URL_ID, alarmLevel.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AlarmLevel> alarmLevelList = alarmLevelRepository.findAll();
        assertThat(alarmLevelList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(alarmLevelSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchAlarmLevel() throws Exception {
        // Initialize the database
        alarmLevel = alarmLevelRepository.saveAndFlush(alarmLevel);
        alarmLevelSearchRepository.save(alarmLevel);

        // Search the alarmLevel
        restAlarmLevelMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + alarmLevel.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alarmLevel.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].color").value(hasItem(DEFAULT_COLOR)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }
}
