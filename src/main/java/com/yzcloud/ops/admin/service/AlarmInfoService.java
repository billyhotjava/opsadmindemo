package com.yzcloud.ops.admin.service;

import com.yzcloud.ops.admin.domain.AlarmInfo;
import com.yzcloud.ops.admin.repository.AlarmInfoRepository;
import com.yzcloud.ops.admin.repository.spec.AlarmInfoSpecification;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class AlarmInfoService {

    @Autowired
    private AlarmInfoRepository alarmInfoRepository;

    public List<AlarmInfo> findAlarmInfos(
        Long alarmRuleId,
        Long eventRuleId,
        Long alarmLevelId,
        Long categoryId,
        String alarmInfoKeywords
    ) {
        Specification<AlarmInfo> spec = new AlarmInfoSpecification(alarmRuleId, eventRuleId, alarmLevelId, categoryId, alarmInfoKeywords);
        return alarmInfoRepository.findAll(spec);
    }
}
