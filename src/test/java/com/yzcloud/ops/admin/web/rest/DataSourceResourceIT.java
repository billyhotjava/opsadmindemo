package com.yzcloud.ops.admin.web.rest;

import static com.yzcloud.ops.admin.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.yzcloud.ops.admin.IntegrationTest;
import com.yzcloud.ops.admin.domain.DataSource;
import com.yzcloud.ops.admin.repository.DataSourceRepository;
import com.yzcloud.ops.admin.repository.search.DataSourceSearchRepository;
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
 * Integration tests for the {@link DataSourceResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DataSourceResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ALIAS = "AAAAAAAAAA";
    private static final String UPDATED_ALIAS = "BBBBBBBBBB";

    private static final String DEFAULT_SOURCE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_SOURCE_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final String DEFAULT_CREDENTIAL = "AAAAAAAAAA";
    private static final String UPDATED_CREDENTIAL = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_MODIFIED_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_MODIFIED_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_MODIFIED_BY = "AAAAAAAAAA";
    private static final String UPDATED_MODIFIED_BY = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_USED = false;
    private static final Boolean UPDATED_IS_USED = true;

    private static final String ENTITY_API_URL = "/api/data-sources";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/data-sources";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private DataSourceSearchRepository dataSourceSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDataSourceMockMvc;

    private DataSource dataSource;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DataSource createEntity(EntityManager em) {
        DataSource dataSource = new DataSource()
            .name(DEFAULT_NAME)
            .alias(DEFAULT_ALIAS)
            .sourceType(DEFAULT_SOURCE_TYPE)
            .description(DEFAULT_DESCRIPTION)
            .url(DEFAULT_URL)
            .credential(DEFAULT_CREDENTIAL)
            .createdTime(DEFAULT_CREATED_TIME)
            .createdBy(DEFAULT_CREATED_BY)
            .modifiedTime(DEFAULT_MODIFIED_TIME)
            .modifiedBy(DEFAULT_MODIFIED_BY)
            .isUsed(DEFAULT_IS_USED);
        return dataSource;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DataSource createUpdatedEntity(EntityManager em) {
        DataSource dataSource = new DataSource()
            .name(UPDATED_NAME)
            .alias(UPDATED_ALIAS)
            .sourceType(UPDATED_SOURCE_TYPE)
            .description(UPDATED_DESCRIPTION)
            .url(UPDATED_URL)
            .credential(UPDATED_CREDENTIAL)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY)
            .isUsed(UPDATED_IS_USED);
        return dataSource;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        dataSourceSearchRepository.deleteAll();
        assertThat(dataSourceSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        dataSource = createEntity(em);
    }

    @Test
    @Transactional
    void createDataSource() throws Exception {
        int databaseSizeBeforeCreate = dataSourceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        // Create the DataSource
        restDataSourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dataSource)))
            .andExpect(status().isCreated());

        // Validate the DataSource in the database
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        DataSource testDataSource = dataSourceList.get(dataSourceList.size() - 1);
        assertThat(testDataSource.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testDataSource.getAlias()).isEqualTo(DEFAULT_ALIAS);
        assertThat(testDataSource.getSourceType()).isEqualTo(DEFAULT_SOURCE_TYPE);
        assertThat(testDataSource.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testDataSource.getUrl()).isEqualTo(DEFAULT_URL);
        assertThat(testDataSource.getCredential()).isEqualTo(DEFAULT_CREDENTIAL);
        assertThat(testDataSource.getCreatedTime()).isEqualTo(DEFAULT_CREATED_TIME);
        assertThat(testDataSource.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testDataSource.getModifiedTime()).isEqualTo(DEFAULT_MODIFIED_TIME);
        assertThat(testDataSource.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
        assertThat(testDataSource.getIsUsed()).isEqualTo(DEFAULT_IS_USED);
    }

    @Test
    @Transactional
    void createDataSourceWithExistingId() throws Exception {
        // Create the DataSource with an existing ID
        dataSource.setId(1L);

        int databaseSizeBeforeCreate = dataSourceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restDataSourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dataSource)))
            .andExpect(status().isBadRequest());

        // Validate the DataSource in the database
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllDataSources() throws Exception {
        // Initialize the database
        dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList
        restDataSourceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dataSource.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].alias").value(hasItem(DEFAULT_ALIAS)))
            .andExpect(jsonPath("$.[*].sourceType").value(hasItem(DEFAULT_SOURCE_TYPE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].credential").value(hasItem(DEFAULT_CREDENTIAL)))
            .andExpect(jsonPath("$.[*].createdTime").value(hasItem(sameInstant(DEFAULT_CREATED_TIME))))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedTime").value(hasItem(sameInstant(DEFAULT_MODIFIED_TIME))))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)))
            .andExpect(jsonPath("$.[*].isUsed").value(hasItem(DEFAULT_IS_USED.booleanValue())));
    }

    @Test
    @Transactional
    void getDataSource() throws Exception {
        // Initialize the database
        dataSourceRepository.saveAndFlush(dataSource);

        // Get the dataSource
        restDataSourceMockMvc
            .perform(get(ENTITY_API_URL_ID, dataSource.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(dataSource.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.alias").value(DEFAULT_ALIAS))
            .andExpect(jsonPath("$.sourceType").value(DEFAULT_SOURCE_TYPE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL))
            .andExpect(jsonPath("$.credential").value(DEFAULT_CREDENTIAL))
            .andExpect(jsonPath("$.createdTime").value(sameInstant(DEFAULT_CREATED_TIME)))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.modifiedTime").value(sameInstant(DEFAULT_MODIFIED_TIME)))
            .andExpect(jsonPath("$.modifiedBy").value(DEFAULT_MODIFIED_BY))
            .andExpect(jsonPath("$.isUsed").value(DEFAULT_IS_USED.booleanValue()));
    }

    @Test
    @Transactional
    void getNonExistingDataSource() throws Exception {
        // Get the dataSource
        restDataSourceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDataSource() throws Exception {
        // Initialize the database
        dataSourceRepository.saveAndFlush(dataSource);

        int databaseSizeBeforeUpdate = dataSourceRepository.findAll().size();
        dataSourceSearchRepository.save(dataSource);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());

        // Update the dataSource
        DataSource updatedDataSource = dataSourceRepository.findById(dataSource.getId()).get();
        // Disconnect from session so that the updates on updatedDataSource are not directly saved in db
        em.detach(updatedDataSource);
        updatedDataSource
            .name(UPDATED_NAME)
            .alias(UPDATED_ALIAS)
            .sourceType(UPDATED_SOURCE_TYPE)
            .description(UPDATED_DESCRIPTION)
            .url(UPDATED_URL)
            .credential(UPDATED_CREDENTIAL)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY)
            .isUsed(UPDATED_IS_USED);

        restDataSourceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedDataSource.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedDataSource))
            )
            .andExpect(status().isOk());

        // Validate the DataSource in the database
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeUpdate);
        DataSource testDataSource = dataSourceList.get(dataSourceList.size() - 1);
        assertThat(testDataSource.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testDataSource.getAlias()).isEqualTo(UPDATED_ALIAS);
        assertThat(testDataSource.getSourceType()).isEqualTo(UPDATED_SOURCE_TYPE);
        assertThat(testDataSource.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testDataSource.getUrl()).isEqualTo(UPDATED_URL);
        assertThat(testDataSource.getCredential()).isEqualTo(UPDATED_CREDENTIAL);
        assertThat(testDataSource.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testDataSource.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testDataSource.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testDataSource.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        assertThat(testDataSource.getIsUsed()).isEqualTo(UPDATED_IS_USED);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<DataSource> dataSourceSearchList = IterableUtils.toList(dataSourceSearchRepository.findAll());
                DataSource testDataSourceSearch = dataSourceSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testDataSourceSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testDataSourceSearch.getAlias()).isEqualTo(UPDATED_ALIAS);
                assertThat(testDataSourceSearch.getSourceType()).isEqualTo(UPDATED_SOURCE_TYPE);
                assertThat(testDataSourceSearch.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
                assertThat(testDataSourceSearch.getUrl()).isEqualTo(UPDATED_URL);
                assertThat(testDataSourceSearch.getCredential()).isEqualTo(UPDATED_CREDENTIAL);
                assertThat(testDataSourceSearch.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
                assertThat(testDataSourceSearch.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
                assertThat(testDataSourceSearch.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
                assertThat(testDataSourceSearch.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
                assertThat(testDataSourceSearch.getIsUsed()).isEqualTo(UPDATED_IS_USED);
            });
    }

    @Test
    @Transactional
    void putNonExistingDataSource() throws Exception {
        int databaseSizeBeforeUpdate = dataSourceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        dataSource.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dataSource.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(dataSource))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataSource in the database
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchDataSource() throws Exception {
        int databaseSizeBeforeUpdate = dataSourceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        dataSource.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(dataSource))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataSource in the database
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDataSource() throws Exception {
        int databaseSizeBeforeUpdate = dataSourceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        dataSource.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dataSource)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DataSource in the database
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateDataSourceWithPatch() throws Exception {
        // Initialize the database
        dataSourceRepository.saveAndFlush(dataSource);

        int databaseSizeBeforeUpdate = dataSourceRepository.findAll().size();

        // Update the dataSource using partial update
        DataSource partialUpdatedDataSource = new DataSource();
        partialUpdatedDataSource.setId(dataSource.getId());

        partialUpdatedDataSource
            .description(UPDATED_DESCRIPTION)
            .url(UPDATED_URL)
            .createdTime(UPDATED_CREATED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY)
            .isUsed(UPDATED_IS_USED);

        restDataSourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDataSource.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDataSource))
            )
            .andExpect(status().isOk());

        // Validate the DataSource in the database
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeUpdate);
        DataSource testDataSource = dataSourceList.get(dataSourceList.size() - 1);
        assertThat(testDataSource.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testDataSource.getAlias()).isEqualTo(DEFAULT_ALIAS);
        assertThat(testDataSource.getSourceType()).isEqualTo(DEFAULT_SOURCE_TYPE);
        assertThat(testDataSource.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testDataSource.getUrl()).isEqualTo(UPDATED_URL);
        assertThat(testDataSource.getCredential()).isEqualTo(DEFAULT_CREDENTIAL);
        assertThat(testDataSource.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testDataSource.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testDataSource.getModifiedTime()).isEqualTo(DEFAULT_MODIFIED_TIME);
        assertThat(testDataSource.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        assertThat(testDataSource.getIsUsed()).isEqualTo(UPDATED_IS_USED);
    }

    @Test
    @Transactional
    void fullUpdateDataSourceWithPatch() throws Exception {
        // Initialize the database
        dataSourceRepository.saveAndFlush(dataSource);

        int databaseSizeBeforeUpdate = dataSourceRepository.findAll().size();

        // Update the dataSource using partial update
        DataSource partialUpdatedDataSource = new DataSource();
        partialUpdatedDataSource.setId(dataSource.getId());

        partialUpdatedDataSource
            .name(UPDATED_NAME)
            .alias(UPDATED_ALIAS)
            .sourceType(UPDATED_SOURCE_TYPE)
            .description(UPDATED_DESCRIPTION)
            .url(UPDATED_URL)
            .credential(UPDATED_CREDENTIAL)
            .createdTime(UPDATED_CREATED_TIME)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedTime(UPDATED_MODIFIED_TIME)
            .modifiedBy(UPDATED_MODIFIED_BY)
            .isUsed(UPDATED_IS_USED);

        restDataSourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDataSource.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDataSource))
            )
            .andExpect(status().isOk());

        // Validate the DataSource in the database
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeUpdate);
        DataSource testDataSource = dataSourceList.get(dataSourceList.size() - 1);
        assertThat(testDataSource.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testDataSource.getAlias()).isEqualTo(UPDATED_ALIAS);
        assertThat(testDataSource.getSourceType()).isEqualTo(UPDATED_SOURCE_TYPE);
        assertThat(testDataSource.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testDataSource.getUrl()).isEqualTo(UPDATED_URL);
        assertThat(testDataSource.getCredential()).isEqualTo(UPDATED_CREDENTIAL);
        assertThat(testDataSource.getCreatedTime()).isEqualTo(UPDATED_CREATED_TIME);
        assertThat(testDataSource.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testDataSource.getModifiedTime()).isEqualTo(UPDATED_MODIFIED_TIME);
        assertThat(testDataSource.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        assertThat(testDataSource.getIsUsed()).isEqualTo(UPDATED_IS_USED);
    }

    @Test
    @Transactional
    void patchNonExistingDataSource() throws Exception {
        int databaseSizeBeforeUpdate = dataSourceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        dataSource.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, dataSource.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(dataSource))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataSource in the database
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDataSource() throws Exception {
        int databaseSizeBeforeUpdate = dataSourceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        dataSource.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(dataSource))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataSource in the database
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDataSource() throws Exception {
        int databaseSizeBeforeUpdate = dataSourceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        dataSource.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(dataSource))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the DataSource in the database
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteDataSource() throws Exception {
        // Initialize the database
        dataSourceRepository.saveAndFlush(dataSource);
        dataSourceRepository.save(dataSource);
        dataSourceSearchRepository.save(dataSource);

        int databaseSizeBeforeDelete = dataSourceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the dataSource
        restDataSourceMockMvc
            .perform(delete(ENTITY_API_URL_ID, dataSource.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<DataSource> dataSourceList = dataSourceRepository.findAll();
        assertThat(dataSourceList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dataSourceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchDataSource() throws Exception {
        // Initialize the database
        dataSource = dataSourceRepository.saveAndFlush(dataSource);
        dataSourceSearchRepository.save(dataSource);

        // Search the dataSource
        restDataSourceMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + dataSource.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dataSource.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].alias").value(hasItem(DEFAULT_ALIAS)))
            .andExpect(jsonPath("$.[*].sourceType").value(hasItem(DEFAULT_SOURCE_TYPE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].credential").value(hasItem(DEFAULT_CREDENTIAL)))
            .andExpect(jsonPath("$.[*].createdTime").value(hasItem(sameInstant(DEFAULT_CREATED_TIME))))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedTime").value(hasItem(sameInstant(DEFAULT_MODIFIED_TIME))))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)))
            .andExpect(jsonPath("$.[*].isUsed").value(hasItem(DEFAULT_IS_USED.booleanValue())));
    }
}
