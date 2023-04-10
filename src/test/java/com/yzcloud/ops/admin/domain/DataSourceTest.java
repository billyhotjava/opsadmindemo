package com.yzcloud.ops.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.yzcloud.ops.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DataSourceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DataSource.class);
        DataSource dataSource1 = new DataSource();
        dataSource1.setId(1L);
        DataSource dataSource2 = new DataSource();
        dataSource2.setId(dataSource1.getId());
        assertThat(dataSource1).isEqualTo(dataSource2);
        dataSource2.setId(2L);
        assertThat(dataSource1).isNotEqualTo(dataSource2);
        dataSource1.setId(null);
        assertThat(dataSource1).isNotEqualTo(dataSource2);
    }
}
