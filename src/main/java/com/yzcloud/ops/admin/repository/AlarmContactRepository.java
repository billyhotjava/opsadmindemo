package com.yzcloud.ops.admin.repository;

import com.yzcloud.ops.admin.domain.AlarmContact;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AlarmContact entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AlarmContactRepository extends JpaRepository<AlarmContact, Long> {}
