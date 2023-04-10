package com.yzcloud.ops.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.yzcloud.ops.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlarmContactTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlarmContact.class);
        AlarmContact alarmContact1 = new AlarmContact();
        alarmContact1.setId(1L);
        AlarmContact alarmContact2 = new AlarmContact();
        alarmContact2.setId(alarmContact1.getId());
        assertThat(alarmContact1).isEqualTo(alarmContact2);
        alarmContact2.setId(2L);
        assertThat(alarmContact1).isNotEqualTo(alarmContact2);
        alarmContact1.setId(null);
        assertThat(alarmContact1).isNotEqualTo(alarmContact2);
    }
}
