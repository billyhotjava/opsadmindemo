package com.yzcloud.ops.admin.repository;

import com.yzcloud.ops.admin.domain.EventRule;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the EventRule entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EventRuleRepository extends JpaRepository<EventRule, Long> {}
