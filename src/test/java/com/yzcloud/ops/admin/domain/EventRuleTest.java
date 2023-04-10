package com.yzcloud.ops.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.yzcloud.ops.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EventRuleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(EventRule.class);
        EventRule eventRule1 = new EventRule();
        eventRule1.setId(1L);
        EventRule eventRule2 = new EventRule();
        eventRule2.setId(eventRule1.getId());
        assertThat(eventRule1).isEqualTo(eventRule2);
        eventRule2.setId(2L);
        assertThat(eventRule1).isNotEqualTo(eventRule2);
        eventRule1.setId(null);
        assertThat(eventRule1).isNotEqualTo(eventRule2);
    }
}
