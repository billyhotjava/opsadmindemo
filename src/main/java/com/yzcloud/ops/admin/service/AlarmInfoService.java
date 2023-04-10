package com.yzcloud.ops.admin.service;

import com.yzcloud.ops.admin.domain.AlarmInfo;
import com.yzcloud.ops.admin.repository.AlarmInfoRepository;
import com.yzcloud.ops.admin.repository.spec.AlarmInfoSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class AlarmInfoService {

    @Autowired
    private AlarmInfoRepository alarmInfoRepository;

    public Page<AlarmInfo> findAllByConditions(
        Long alarmRuleId,
        Long eventRuleId,
        Long alarmLevelId,
        Long categoryId,
        String alarmInfoKeywords,
        Pageable pageable
    ) {
        AlarmInfoSpecification spec = new AlarmInfoSpecification(alarmRuleId, eventRuleId, alarmLevelId, categoryId, alarmInfoKeywords);
        return alarmInfoRepository.findAll(spec, pageable);
    }
}
