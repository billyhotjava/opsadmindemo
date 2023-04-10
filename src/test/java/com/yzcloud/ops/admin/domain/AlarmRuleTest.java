package com.yzcloud.ops.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.yzcloud.ops.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlarmRuleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlarmRule.class);
        AlarmRule alarmRule1 = new AlarmRule();
        alarmRule1.setId(1L);
        AlarmRule alarmRule2 = new AlarmRule();
        alarmRule2.setId(alarmRule1.getId());
        assertThat(alarmRule1).isEqualTo(alarmRule2);
        alarmRule2.setId(2L);
        assertThat(alarmRule1).isNotEqualTo(alarmRule2);
        alarmRule1.setId(null);
        assertThat(alarmRule1).isNotEqualTo(alarmRule2);
    }
}
