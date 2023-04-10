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
public interface AlarmInfoRepository extends JpaRepository<AlarmInfo, Long>, JpaSpecificationExecutor<AlarmInfo> {}
