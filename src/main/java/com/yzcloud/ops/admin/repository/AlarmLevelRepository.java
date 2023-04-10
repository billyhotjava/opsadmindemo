package com.yzcloud.ops.admin.repository;

import com.yzcloud.ops.admin.domain.AlarmLevel;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AlarmLevel entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AlarmLevelRepository extends JpaRepository<AlarmLevel, Long> {}
