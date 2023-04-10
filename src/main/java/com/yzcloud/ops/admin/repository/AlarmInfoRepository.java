package com.yzcloud.ops.admin.repository;

import com.yzcloud.ops.admin.domain.AlarmInfo;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AlarmInfo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AlarmInfoRepository extends JpaRepository<AlarmInfo, Long> {
    @Query(
        nativeQuery = true,
        value = " select t1.* \n" +
        " from alarm_info t1\n" +
        " join alarm_rule t2 on t1.alarm_rule_id = t2.id\n" +
        " join event_rule t3 on t2.event_rule_id = t3.id\n" +
        " join category t4 on t3.category_id = t4.id\n" +
        " where t2.id  = :alarmRuleId\n" +
        " and t3.id  = :eventRuleId\n" +
        " and t4.id = :categoryId"
    )
    List<AlarmInfo> findAllAlarmInfoByQuery(
        @Param("alarmRuleId") Long alarmRuleId,
        @Param("eventRuleId") Long eventRuleId,
        @Param("categoryId") Long categoryId
    );
}
