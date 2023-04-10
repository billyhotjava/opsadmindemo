package com.yzcloud.ops.admin.repository;

import com.yzcloud.ops.admin.domain.AlarmRule;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AlarmRule entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AlarmRuleRepository extends JpaRepository<AlarmRule, Long> {}
