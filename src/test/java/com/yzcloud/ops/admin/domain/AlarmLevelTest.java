package com.yzcloud.ops.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.yzcloud.ops.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlarmLevelTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlarmLevel.class);
        AlarmLevel alarmLevel1 = new AlarmLevel();
        alarmLevel1.setId(1L);
        AlarmLevel alarmLevel2 = new AlarmLevel();
        alarmLevel2.setId(alarmLevel1.getId());
        assertThat(alarmLevel1).isEqualTo(alarmLevel2);
        alarmLevel2.setId(2L);
        assertThat(alarmLevel1).isNotEqualTo(alarmLevel2);
        alarmLevel1.setId(null);
        assertThat(alarmLevel1).isNotEqualTo(alarmLevel2);
    }
}
